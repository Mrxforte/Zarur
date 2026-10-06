package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.repository.AuthRepository
import com.example.zarur.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        fullName: String,
        nickname: String,
        email: String,
        phone: String,
        gender: String = "",
        dob: String = "",
        imageUri: String? = null
    ): Flow<Resource<Unit>> = flow {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            emit(Resource.Error("User not logged in"))
            return@flow
        }
        emit(Resource.Loading)

        var avatarUrl = imageUri
        if (imageUri != null && (imageUri.startsWith("content://") || imageUri.startsWith("file://"))) {
            var uploadError: String? = null
            userRepository.uploadAvatar(uid, imageUri).collect { uploadRes ->
                when (uploadRes) {
                    is Resource.Success -> {
                        avatarUrl = uploadRes.data
                    }
                    is Resource.Error -> {
                        uploadError = uploadRes.message
                    }
                    is Resource.Loading -> {
                        // In progress
                    }
                }
            }
            if (uploadError != null) {
                emit(Resource.Error("Failed to upload avatar: $uploadError"))
                return@flow
            }
        }

        val updatedUser = User(
            id = uid,
            fullName = fullName,
            nickname = nickname,
            email = email,
            phone = phone,
            gender = gender,
            dob = dob,
            avatarUrl = avatarUrl
        )

        userRepository.updateUserProfile(updatedUser).collect { res ->
            emit(res)
        }
    }
}
