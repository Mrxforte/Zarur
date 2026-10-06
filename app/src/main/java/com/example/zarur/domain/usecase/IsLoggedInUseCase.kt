package com.example.zarur.domain.usecase

import com.example.zarur.data.local.AppPreferences
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val appPreferences: AppPreferences
) {
    operator fun invoke(): Boolean {
        return appPreferences.isLoggedIn
    }
}
