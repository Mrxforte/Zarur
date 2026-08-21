package com.example.zarur.data.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("zarur_prefs", Context.MODE_PRIVATE)

    var isFirstTimeLaunch: Boolean
        get() = prefs.getBoolean("is_first_time", true)
        set(value) = prefs.edit().putBoolean("is_first_time", value).apply()
}
