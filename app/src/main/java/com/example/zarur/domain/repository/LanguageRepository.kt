package com.example.zarur.domain.repository

import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    fun getLanguage(): Flow<String>
    suspend fun setLanguage(languageCode: String)
}
