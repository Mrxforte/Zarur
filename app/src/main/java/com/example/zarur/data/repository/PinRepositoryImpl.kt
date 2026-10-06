package com.example.zarur.data.repository

import android.content.SharedPreferences
import com.example.zarur.domain.repository.PinRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinRepositoryImpl @Inject constructor(
    private val securePrefs: SharedPreferences
) : PinRepository {

    private val KEY_PIN = "user_pin"

    override suspend fun savePin(pin: String) {
        securePrefs.edit().putString(KEY_PIN, pin).apply()
    }

    override suspend fun validatePin(pin: String): Boolean {
        val savedPin = securePrefs.getString(KEY_PIN, null)
        return savedPin == pin
    }

    override suspend fun isPinSet(): Boolean {
        return securePrefs.contains(KEY_PIN)
    }
}
