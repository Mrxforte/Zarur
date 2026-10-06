package com.example.zarur.domain.usecase

import com.example.zarur.domain.repository.PinRepository
import javax.inject.Inject

class ValidatePinUseCase @Inject constructor(
    private val repository: PinRepository
) {
    suspend operator fun invoke(pin: String): Boolean = repository.validatePin(pin)
}
