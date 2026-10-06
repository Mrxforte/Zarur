package com.example.zarur.domain.usecase

import com.example.zarur.domain.repository.ThemeRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val repository: ThemeRepository
) {
    suspend operator fun invoke(isDark: Boolean) = repository.setDarkMode(isDark)
}
