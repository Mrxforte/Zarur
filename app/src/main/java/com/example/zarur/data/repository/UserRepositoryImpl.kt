package com.example.zarur.data.repository

import android.content.Context
import android.net.Uri
import com.example.zarur.data.local.AppPreferences
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val appPreferences: AppPreferences
) : UserRepository {

    override fun getUserProfile(userId: String): Flow<Resource<User?>> = flow {
        emit(Resource.Loading)
        val currentUser = firebaseAuth.currentUser
        try {
            val docSnapshot = firestore.collection("users").document(userId).get().await()
            val fullName = (docSnapshot.getString("fullName") ?: currentUser?.displayName ?: "").ifBlank { appPreferences.userName }
            val nickname = (docSnapshot.getString("nickname") ?: "").ifBlank { appPreferences.userNickname }
            val email = (docSnapshot.getString("email") ?: currentUser?.email ?: "").ifBlank { appPreferences.userEmail }
            val phone = (docSnapshot.getString("phone") ?: currentUser?.phoneNumber ?: "").ifBlank { appPreferences.userPhone }
            val gender = (docSnapshot.getString("gender") ?: "").ifBlank { appPreferences.userGender }
            val dob = (docSnapshot.getString("dob") ?: "").ifBlank { appPreferences.userDob }
            val avatarUrl = (docSnapshot.getString("avatarUrl") ?: currentUser?.photoUrl?.toString() ?: "").ifBlank { appPreferences.userAvatarUrl }

            appPreferences.userId = userId
            if (fullName.isNotBlank()) appPreferences.userName = fullName
            if (nickname.isNotBlank()) appPreferences.userNickname = nickname
            if (email.isNotBlank()) appPreferences.userEmail = email
            if (phone.isNotBlank()) appPreferences.userPhone = phone
            if (gender.isNotBlank()) appPreferences.userGender = gender
            if (dob.isNotBlank()) appPreferences.userDob = dob
            if (avatarUrl.isNotBlank()) appPreferences.userAvatarUrl = avatarUrl

            val user = User(
                id = userId,
                fullName = fullName,
                nickname = nickname,
                email = email,
                phone = phone,
                gender = gender,
                dob = dob,
                avatarUrl = avatarUrl
            )
            emit(Resource.Success(user))
        } catch (e: Exception) {
            val cachedUser = User(
                id = userId,
                fullName = (currentUser?.displayName ?: "").ifBlank { appPreferences.userName },
                nickname = appPreferences.userNickname,
                email = (currentUser?.email ?: "").ifBlank { appPreferences.userEmail },
                phone = (currentUser?.phoneNumber ?: "").ifBlank { appPreferences.userPhone },
                gender = appPreferences.userGender,
                dob = appPreferences.userDob,
                avatarUrl = (currentUser?.photoUrl?.toString() ?: "").ifBlank { appPreferences.userAvatarUrl }
            )
            emit(Resource.Success(cachedUser))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateUserProfile(user: User): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)

        appPreferences.userId = user.id
        if (user.fullName.isNotBlank()) appPreferences.userName = user.fullName
        if (user.nickname.isNotBlank()) appPreferences.userNickname = user.nickname
        if (user.email.isNotBlank()) appPreferences.userEmail = user.email
        if (user.phone.isNotBlank()) appPreferences.userPhone = user.phone
        if (user.gender.isNotBlank()) appPreferences.userGender = user.gender
        if (user.dob.isNotBlank()) appPreferences.userDob = user.dob
        if (!user.avatarUrl.isNullOrEmpty()) appPreferences.userAvatarUrl = user.avatarUrl

        try {
            val userMap = mapOf(
                "id" to user.id,
                "fullName" to user.fullName,
                "nickname" to user.nickname,
                "email" to user.email,
                "phone" to user.phone,
                "gender" to user.gender,
                "dob" to user.dob,
                "avatarUrl" to (user.avatarUrl ?: "")
            )
            firestore.collection("users").document(user.id).set(userMap).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Success(Unit))
        }
    }.flowOn(Dispatchers.IO)

    override fun uploadAvatar(userId: String, imageUri: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            var downloadUrl = imageUri
            if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                try {
                    val uri = Uri.parse(imageUri)
                    val storageRef = firebaseStorage.reference.child("avatars/$userId.jpg")
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        storageRef.putStream(inputStream).await()
                        inputStream.close()
                        downloadUrl = storageRef.downloadUrl.await().toString()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            appPreferences.userAvatarUrl = downloadUrl

            try {
                firestore.collection("users").document(userId)
                    .update("avatarUrl", downloadUrl).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            emit(Resource.Success(downloadUrl))
        } catch (e: Exception) {
            appPreferences.userAvatarUrl = imageUri
            emit(Resource.Success(imageUri))
        }
    }.flowOn(Dispatchers.IO)
}
