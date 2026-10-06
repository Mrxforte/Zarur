package com.example.zarur.domain.usecase

import com.example.zarur.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isDarkMode()
}
