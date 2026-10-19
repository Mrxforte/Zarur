package com.example.zarur.data.repository

import com.example.zarur.data.local.AppPreferences
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import android.content.Context
import com.example.zarur.R
import com.example.zarur.presentation.service.WelcomeNotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val appPreferences: AppPreferences
) : AuthRepository {

    override fun signUp(email: String, password: String, fullName: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val uid = try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                authResult.user?.uid ?: throw Exception("User creation failed")
            } catch (e: Exception) {
                if (e is FirebaseAuthUserCollisionException || e.message?.contains("email-already-in-use", ignoreCase = true) == true) {
                    val signInResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                    signInResult.user?.uid ?: throw e
                } else {
                    throw e
                }
            }

            val user = User(
                id = uid,
                fullName = fullName,
                email = email,
                createdAt = System.currentTimeMillis()
            )

            try {
                val userMap = mapOf(
                    "id" to user.id,
                    "fullName" to user.fullName,
                    "nickname" to user.nickname,
                    "email" to user.email,
                    "phone" to user.phone,
                    "gender" to user.gender,
                    "dob" to user.dob,
                    "avatarUrl" to (user.avatarUrl ?: ""),
                    "createdAt" to user.createdAt
                )
                firestore.collection("users").document(uid).set(userMap).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            appPreferences.isLoggedIn = true
            appPreferences.isTestMode = false
            appPreferences.userId = user.id
            if (user.fullName.isNotBlank()) appPreferences.userName = user.fullName
            if (user.email.isNotBlank()) appPreferences.userEmail = user.email
            WelcomeNotificationScheduler.scheduleWelcomeNotification(context, appPreferences)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            e.printStackTrace()
            // Firebase ulanishida haqiqiy xatoni ko'rsatish uchun fallback olib tashlandi:
            emit(Resource.Error(parseAuthError(e)))
        }
    }.flowOn(Dispatchers.IO)

    override fun signIn(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Sign in failed")

            var user = User(id = uid, email = email)
            try {
                val docSnapshot = firestore.collection("users").document(uid).get().await()
                if (docSnapshot.exists()) {
                    user = User(
                        id = uid,
                        fullName = docSnapshot.getString("fullName") ?: "",
                        nickname = docSnapshot.getString("nickname") ?: "",
                        email = docSnapshot.getString("email") ?: email,
                        phone = docSnapshot.getString("phone") ?: "",
                        gender = docSnapshot.getString("gender") ?: "",
                        dob = docSnapshot.getString("dob") ?: "",
                        avatarUrl = docSnapshot.getString("avatarUrl")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            appPreferences.isLoggedIn = true
            appPreferences.isTestMode = false
            appPreferences.userId = user.id
            if (user.fullName.isNotBlank()) appPreferences.userName = user.fullName
            if (user.nickname.isNotBlank()) appPreferences.userNickname = user.nickname
            if (user.email.isNotBlank()) appPreferences.userEmail = user.email
            if (user.phone.isNotBlank()) appPreferences.userPhone = user.phone
            if (user.gender.isNotBlank()) appPreferences.userGender = user.gender
            if (user.dob.isNotBlank()) appPreferences.userDob = user.dob
            if (!user.avatarUrl.isNullOrEmpty()) appPreferences.userAvatarUrl = user.avatarUrl

            WelcomeNotificationScheduler.scheduleWelcomeNotification(context, appPreferences)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            e.printStackTrace()
            // Firebase ulanishida haqiqiy xatoni ko'rsatish uchun fallback olib tashlandi:
            emit(Resource.Error(parseAuthError(e)))
        }
    }.flowOn(Dispatchers.IO)

    override fun signOut() {
        firebaseAuth.signOut()
        appPreferences.clearUserData()
    }

    override fun updatePassword(newPassword: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val user = firebaseAuth.currentUser ?: throw Exception("User not authenticated")
            user.updatePassword(newPassword).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(parseAuthError(e)))
        }
    }.flowOn(Dispatchers.IO)

    override fun getCurrentUserId(): String? {
        val uid = firebaseAuth.currentUser?.uid
        if (!uid.isNullOrBlank()) {
            appPreferences.isLoggedIn = true
            appPreferences.userId = uid
            return uid
        }
        if (appPreferences.isLoggedIn || appPreferences.isTestMode || appPreferences.userId.isNotBlank()) {
            appPreferences.isLoggedIn = true
            if (appPreferences.userId.isBlank()) {
                appPreferences.userId = "user_" + System.currentTimeMillis()
            }
            return appPreferences.userId
        }
        // Remember local session and auto-generate persistent user ID if needed
        appPreferences.isLoggedIn = true
        if (appPreferences.userId.isBlank()) {
            appPreferences.userId = "user_" + System.currentTimeMillis()
        }
        return appPreferences.userId
    }

    override fun getCurrentUser(): Flow<Resource<User?>> = flow {
        val uid = getCurrentUserId()
        if (uid == null) {
            emit(Resource.Success(null))
            return@flow
        }
        emit(Resource.Loading)
        try {
            val docSnapshot = firestore.collection("users").document(uid).get().await()
            if (docSnapshot.exists()) {
                val user = User(
                    id = uid,
                    fullName = docSnapshot.getString("fullName") ?: "",
                    nickname = docSnapshot.getString("nickname") ?: "",
                    email = docSnapshot.getString("email") ?: (firebaseAuth.currentUser?.email ?: ""),
                    phone = docSnapshot.getString("phone") ?: "",
                    gender = docSnapshot.getString("gender") ?: "",
                    dob = docSnapshot.getString("dob") ?: "",
                    avatarUrl = docSnapshot.getString("avatarUrl")
                )
                emit(Resource.Success(user))
            } else {
                val fallbackUser = User(
                    id = uid,
                    fullName = appPreferences.userName.ifBlank { "Foydalanuvchi" },
                    email = appPreferences.userEmail.ifBlank { firebaseAuth.currentUser?.email ?: "" }
                )
                emit(Resource.Success(fallbackUser))
            }
        } catch (e: Exception) {
            val fallbackUser = User(
                id = uid,
                fullName = appPreferences.userName.ifBlank { "Foydalanuvchi" },
                email = appPreferences.userEmail.ifBlank { firebaseAuth.currentUser?.email ?: "" }
            )
            emit(Resource.Success(fallbackUser))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseAuthError(e: Throwable): String {
        val message = e.message ?: ""
        val localizedMsg = e.localizedMessage ?: ""
        val fullErr = "$e $message $localizedMsg"

        return when {
            e is FirebaseAuthUserCollisionException ||
            fullErr.contains("email-already-in-use", ignoreCase = true) ||
            fullErr.contains("already in use", ignoreCase = true) ||
            fullErr.contains("ERROR_EMAIL_ALREADY_IN_USE", ignoreCase = true) -> {
                context.getString(R.string.error_email_already_in_use)
            }

            fullErr.contains("WRONG_PASSWORD", ignoreCase = true) ||
            fullErr.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            fullErr.contains("ERROR_WRONG_PASSWORD", ignoreCase = true) ||
            fullErr.contains("invalid credential", ignoreCase = true) ||
            fullErr.contains("invalid-credential", ignoreCase = true) -> {
                context.getString(R.string.error_invalid_credentials)
            }

            fullErr.contains("badly formatted", ignoreCase = true) ||
            fullErr.contains("invalid-email", ignoreCase = true) ||
            fullErr.contains("ERROR_INVALID_EMAIL", ignoreCase = true) -> {
                context.getString(R.string.error_invalid_email_format)
            }

            e is FirebaseAuthInvalidUserException ||
            fullErr.contains("user-not-found", ignoreCase = true) ||
            fullErr.contains("ERROR_USER_NOT_FOUND", ignoreCase = true) ||
            fullErr.contains("no user record", ignoreCase = true) -> {
                context.getString(R.string.error_user_not_found)
            }

            e is FirebaseAuthWeakPasswordException ||
            fullErr.contains("weak-password", ignoreCase = true) ||
            fullErr.contains("ERROR_WEAK_PASSWORD", ignoreCase = true) ||
            fullErr.contains("password is too weak", ignoreCase = true) -> {
                context.getString(R.string.error_weak_password)
            }

            fullErr.contains("too-many-requests", ignoreCase = true) ||
            fullErr.contains("ERROR_TOO_MANY_REQUESTS", ignoreCase = true) -> {
                context.getString(R.string.error_too_many_requests)
            }

            fullErr.contains("user-disabled", ignoreCase = true) ||
            fullErr.contains("ERROR_USER_DISABLED", ignoreCase = true) -> {
                context.getString(R.string.error_account_disabled)
            }

            e is FirebaseNetworkException ||
            e is UnknownHostException ||
            e is SocketTimeoutException ||
            fullErr.contains("network", ignoreCase = true) ||
            fullErr.contains("connection", ignoreCase = true) -> {
                context.getString(R.string.error_network_connection)
            }

            e is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> context.getString(R.string.error_email_already_in_use)
                    "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "INVALID_LOGIN_CREDENTIALS" -> context.getString(
                        R.string.error_invalid_credentials)
                    "ERROR_USER_NOT_FOUND" -> context.getString(R.string.error_user_not_found)
                    "ERROR_USER_DISABLED" -> context.getString(R.string.error_account_disabled)
                    "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.error_too_many_requests)
                    "ERROR_WEAK_PASSWORD" -> context.getString(R.string.error_weak_password)
                    "ERROR_INVALID_EMAIL" -> context.getString(R.string.error_invalid_email_format)
                    else -> localizedMsg.ifBlank { context.getString(R.string.error_unknown_auth) }
                }
            }

            else -> {
                if (localizedMsg.contains("Exception") || localizedMsg.contains("com.google.firebase")) {
                    context.getString(R.string.error_unknown_auth)
                } else if (localizedMsg.isNotBlank()) {
                    localizedMsg
                } else {
                    context.getString(R.string.error_unknown_auth)
                }
            }
        }
    }
}
