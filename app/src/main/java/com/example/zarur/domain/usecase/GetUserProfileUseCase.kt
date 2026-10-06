package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.repository.AuthRepository
import com.example.zarur.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<User?>> {
        val uid = authRepository.getCurrentUserId() ?: return flowOf(Resource.Error("User not authenticated"))
        return userRepository.getUserProfile(uid)
    }
}
