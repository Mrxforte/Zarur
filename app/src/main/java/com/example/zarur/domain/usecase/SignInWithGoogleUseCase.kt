package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(idToken: String): Flow<Resource<User>> {
        return authRepository.signInWithGoogle(idToken)
    }
}
