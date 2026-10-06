package com.example.zarur.domain.repository

import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signUp(email: String, password: String, fullName: String): Flow<Resource<User>>
    fun signIn(email: String, password: String): Flow<Resource<User>>
    fun signInWithGoogle(idToken: String): Flow<Resource<User>>
    fun signOut()
    fun getCurrentUserId(): String?
    fun getCurrentUser(): Flow<Resource<User?>>
}
