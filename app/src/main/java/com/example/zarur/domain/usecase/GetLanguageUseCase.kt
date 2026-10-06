package com.example.zarur.domain.usecase

import com.example.zarur.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLanguageUseCase @Inject constructor(
    private val repository: LanguageRepository
) {
    operator fun invoke(): Flow<String> = repository.getLanguage()
}
