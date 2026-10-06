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
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import android.content.Context
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
            WelcomeNotificationScheduler.scheduleWelcomeNotification(context, appPreferences)
            emit(Resource.Success(user))
        } catch (e: Exception) {
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
            WelcomeNotificationScheduler.scheduleWelcomeNotification(context, appPreferences)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(parseAuthError(e)))
        }
    }.flowOn(Dispatchers.IO)

    override fun signInWithGoogle(idToken: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google Sign in failed")
            val uid = firebaseUser.uid

            var user = User(
                id = uid,
                fullName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                avatarUrl = firebaseUser.photoUrl?.toString(),
                createdAt = System.currentTimeMillis()
            )

            try {
                val docSnapshot = firestore.collection("users").document(uid).get().await()
                if (docSnapshot.exists()) {
                    user = User(
                        id = uid,
                        fullName = docSnapshot.getString("fullName") ?: firebaseUser.displayName ?: "",
                        nickname = docSnapshot.getString("nickname") ?: "",
                        email = docSnapshot.getString("email") ?: firebaseUser.email ?: "",
                        phone = docSnapshot.getString("phone") ?: firebaseUser.phoneNumber ?: "",
                        gender = docSnapshot.getString("gender") ?: "",
                        dob = docSnapshot.getString("dob") ?: "",
                        avatarUrl = docSnapshot.getString("avatarUrl") ?: firebaseUser.photoUrl?.toString()
                    )
                } else {
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            appPreferences.isLoggedIn = true
            appPreferences.isTestMode = false
            WelcomeNotificationScheduler.scheduleWelcomeNotification(context, appPreferences)
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(parseAuthError(e)))
        }
    }.flowOn(Dispatchers.IO)

    override fun signOut() {
        firebaseAuth.signOut()
        appPreferences.isLoggedIn = false
        appPreferences.isTestMode = false
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
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
                    email = firebaseAuth.currentUser?.email ?: ""
                )
                emit(Resource.Success(fallbackUser))
            }
        } catch (e: Exception) {
            emit(Resource.Error(parseAuthError(e)))
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
                "Ushbu email manzili allaqachon ro'yxatdan o'tgan. Iltimos, boshqa email kiriting yoki tizimga kiring."
            }

            fullErr.contains("WRONG_PASSWORD", ignoreCase = true) ||
            fullErr.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            fullErr.contains("ERROR_WRONG_PASSWORD", ignoreCase = true) ||
            fullErr.contains("invalid credential", ignoreCase = true) ||
            fullErr.contains("invalid-credential", ignoreCase = true) -> {
                "Email yoki parol noto'g'ri kiritildi."
            }

            fullErr.contains("badly formatted", ignoreCase = true) ||
            fullErr.contains("invalid-email", ignoreCase = true) ||
            fullErr.contains("ERROR_INVALID_EMAIL", ignoreCase = true) -> {
                "Email manzili noto'g'ri shaklda kiritilgan. Iltimos, qaytadan tekshirib kiriting."
            }

            e is FirebaseAuthInvalidUserException ||
            fullErr.contains("user-not-found", ignoreCase = true) ||
            fullErr.contains("ERROR_USER_NOT_FOUND", ignoreCase = true) ||
            fullErr.contains("no user record", ignoreCase = true) -> {
                "Bunday email manzilli foydalanuvchi topilmadi. Iltimos, avval ro'yxatdan o'ting."
            }

            e is FirebaseAuthWeakPasswordException ||
            fullErr.contains("weak-password", ignoreCase = true) ||
            fullErr.contains("ERROR_WEAK_PASSWORD", ignoreCase = true) ||
            fullErr.contains("password is too weak", ignoreCase = true) -> {
                "Parol juda oddiy. Parol kamida 6 ta belgidan iborat bo'lishi kerak."
            }

            fullErr.contains("too-many-requests", ignoreCase = true) ||
            fullErr.contains("ERROR_TOO_MANY_REQUESTS", ignoreCase = true) -> {
                "Juda ko'p muvaffaqiyatsiz urinishlar. Birozdan so'ng qayta urinib ko'ring."
            }

            fullErr.contains("user-disabled", ignoreCase = true) ||
            fullErr.contains("ERROR_USER_DISABLED", ignoreCase = true) -> {
                "Ushbu hisob bloklangan. Qo'llab-quvvatlash xizmatiga murojaat qiling."
            }

            e is FirebaseNetworkException ||
            e is UnknownHostException ||
            e is SocketTimeoutException ||
            fullErr.contains("network", ignoreCase = true) ||
            fullErr.contains("connection", ignoreCase = true) -> {
                "Internet ulanishida xatolik yuz berdi. Iltimos, tarmoqni tekshiring."
            }

            e is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Ushbu email manzili allaqachon ro'yxatdan o'tgan. Iltimos, boshqa email kiriting yoki tizimga kiring."
                    "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "INVALID_LOGIN_CREDENTIALS" -> "Email yoki parol noto'g'ri kiritildi."
                    "ERROR_USER_NOT_FOUND" -> "Bunday email manzilli foydalanuvchi topilmadi. Iltimos, avval ro'yxatdan o'ting."
                    "ERROR_USER_DISABLED" -> "Ushbu hisob bloklangan."
                    "ERROR_TOO_MANY_REQUESTS" -> "Juda ko'p urinishlar. Birozdan so'ng qayta urinib ko'ring."
                    "ERROR_WEAK_PASSWORD" -> "Parol juda oddiy. Parol kamida 6 ta belgidan iborat bo'lishi kerak."
                    "ERROR_INVALID_EMAIL" -> "Email manzili noto'g'ri shaklda kiritilgan."
                    else -> localizedMsg.ifBlank { "Avtorizatsiyada xatolik yuz berdi." }
                }
            }

            else -> {
                if (localizedMsg.contains("Exception") || localizedMsg.contains("com.google.firebase")) {
                    "Xatolik yuz berdi. Iltimos, qaytadan urinib ko'ring."
                } else if (localizedMsg.isNotBlank()) {
                    localizedMsg
                } else {
                    "Xatolik yuz berdi. Iltimos, qaytadan urinib ko'ring."
                }
            }
        }
    }
}
