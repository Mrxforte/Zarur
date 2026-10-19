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

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()
        
    var isTestMode: Boolean
        get() = prefs.getBoolean("is_test_mode", false)
        set(value) = prefs.edit().putBoolean("is_test_mode", value).apply()

    var userId: String
        get() = prefs.getString("user_id", "") ?: ""
        set(value) = prefs.edit().putString("user_id", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userNickname: String
        get() = prefs.getString("user_nickname", "") ?: ""
        set(value) = prefs.edit().putString("user_nickname", value).apply()

    var userEmail: String
        get() = prefs.getString("user_email", "") ?: ""
        set(value) = prefs.edit().putString("user_email", value).apply()

    var userPhone: String
        get() = prefs.getString("user_phone", "") ?: ""
        set(value) = prefs.edit().putString("user_phone", value).apply()

    var userGender: String
        get() = prefs.getString("user_gender", "") ?: ""
        set(value) = prefs.edit().putString("user_gender", value).apply()

    var userDob: String
        get() = prefs.getString("user_dob", "") ?: ""
        set(value) = prefs.edit().putString("user_dob", value).apply()

    var userAvatarUrl: String
        get() = prefs.getString("user_avatar_url", "") ?: ""
        set(value) = prefs.edit().putString("user_avatar_url", value).apply()

    fun clearUserData() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .putBoolean("is_test_mode", false)
            .remove("user_id")
            .remove("user_name")
            .remove("user_nickname")
            .remove("user_email")
            .remove("user_phone")
            .remove("user_gender")
            .remove("user_dob")
            .remove("user_avatar_url")
            .apply()
    }

    var hasReceivedWelcomeNotification: Boolean
        get() = prefs.getBoolean("has_received_welcome_notif", false)
        set(value) = prefs.edit().putBoolean("has_received_welcome_notif", value).apply()

    var notifGeneral: Boolean
        get() = prefs.getBoolean("notif_general", true)
        set(value) = prefs.edit().putBoolean("notif_general", value).apply()

    var notifSound: Boolean
        get() = prefs.getBoolean("notif_sound", true)
        set(value) = prefs.edit().putBoolean("notif_sound", value).apply()

    var notifVibrate: Boolean
        get() = prefs.getBoolean("notif_vibrate", false)
        set(value) = prefs.edit().putBoolean("notif_vibrate", value).apply()

    var notifSpecialOffers: Boolean
        get() = prefs.getBoolean("notif_special_offers", true)
        set(value) = prefs.edit().putBoolean("notif_special_offers", value).apply()

    var notifPromoDiscount: Boolean
        get() = prefs.getBoolean("notif_promo_discount", false)
        set(value) = prefs.edit().putBoolean("notif_promo_discount", value).apply()

    var notifPayments: Boolean
        get() = prefs.getBoolean("notif_payments", true)
        set(value) = prefs.edit().putBoolean("notif_payments", value).apply()

    var unreadNotificationCount: Int
        get() = prefs.getInt("unread_notif_count", 3)
        set(value) = prefs.edit().putInt("unread_notif_count", value).apply()
}
