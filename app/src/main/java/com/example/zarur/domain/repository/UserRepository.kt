package com.example.zarur.domain.repository

import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(userId: String): Flow<Resource<User?>>
    fun updateUserProfile(user: User): Flow<Resource<Unit>>
    fun uploadAvatar(userId: String, imageUri: String): Flow<Resource<String>>
}
