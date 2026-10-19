package com.example.zarur.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.zarur.domain.repository.LanguageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : LanguageRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    private val KEY_LANGUAGE = "selected_language"

    private fun getCurrentLanguage(): String {
        val saved = prefs.getString(KEY_LANGUAGE, null)
        if (saved != null) return saved

        val sysLang = Locale.getDefault().language
        return if (sysLang == "uz" || sysLang == "en") sysLang else "ru"
    }

    override fun getLanguage(): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_LANGUAGE) {
                trySend(getCurrentLanguage())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getCurrentLanguage())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setLanguage(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).commit()
    }
}
