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
}
