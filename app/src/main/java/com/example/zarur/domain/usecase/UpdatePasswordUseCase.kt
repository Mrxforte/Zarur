package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdatePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(newPassword: String): Flow<Resource<Unit>> {
        return authRepository.updatePassword(newPassword)
    }
}
