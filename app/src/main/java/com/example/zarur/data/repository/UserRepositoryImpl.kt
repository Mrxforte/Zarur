package com.example.zarur.data.repository

import android.content.Context
import android.net.Uri
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.repository.UserRepository
import com.example.zarur.util.parseFirebaseError
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
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : UserRepository {

    override fun getUserProfile(userId: String): Flow<Resource<User?>> = flow {
        emit(Resource.Loading)
        try {
            val docSnapshot = firestore.collection("users").document(userId).get().await()
            if (docSnapshot.exists()) {
                val user = User(
                    id = userId,
                    fullName = docSnapshot.getString("fullName") ?: "",
                    nickname = docSnapshot.getString("nickname") ?: "",
                    email = docSnapshot.getString("email") ?: "",
                    phone = docSnapshot.getString("phone") ?: "",
                    gender = docSnapshot.getString("gender") ?: "",
                    dob = docSnapshot.getString("dob") ?: "",
                    avatarUrl = docSnapshot.getString("avatarUrl")
                )
                emit(Resource.Success(user))
            } else {
                emit(Resource.Success(null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(parseFirebaseError(e)))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateUserProfile(user: User): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
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
            emit(Resource.Error(parseFirebaseError(e)))
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

            firestore.collection("users").document(userId)
                .update("avatarUrl", downloadUrl).await()

            emit(Resource.Success(downloadUrl))
        } catch (e: Exception) {
            emit(Resource.Error(parseFirebaseError(e)))
        }
    }.flowOn(Dispatchers.IO)
}
