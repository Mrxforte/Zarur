package com.example.zarur.domain.usecase

import com.example.zarur.domain.repository.LanguageRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val repository: LanguageRepository
) {
    suspend operator fun invoke(languageCode: String) = repository.setLanguage(languageCode)
}
