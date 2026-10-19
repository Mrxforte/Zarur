package com.example.zarur.domain.usecase

import com.example.zarur.data.local.AppPreferences
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
    private val firebaseAuth: FirebaseAuth
) {
    operator fun invoke(): Boolean {
        if (appPreferences.isTestMode) return true

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            appPreferences.isLoggedIn = true
            if (appPreferences.userId.isBlank()) {
                appPreferences.userId = currentUser.uid
            }
            return true
        }

        // Remember local auth state if previously logged in, user ID is saved
        if (appPreferences.isLoggedIn || appPreferences.userId.isNotBlank()) {
            appPreferences.isLoggedIn = true
            if (appPreferences.userId.isBlank()) {
                appPreferences.userId = "user_" + System.currentTimeMillis()
            }
            return true
        }

        appPreferences.isLoggedIn = false
        return false
    }
}
