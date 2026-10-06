package com.example.zarur.data.local

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AppPreferencesTest {

    private val context: Context = mockk()
    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    private lateinit var appPreferences: AppPreferences

    @Before
    fun setUp() {
        every { context.getSharedPreferences("zarur_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor

        appPreferences = AppPreferences(context)
    }

    @Test
    fun testIsFirstTimeLaunch_getAndSet() {
        every { prefs.getBoolean("is_first_time", true) } returns true
        assertTrue(appPreferences.isFirstTimeLaunch)

        appPreferences.isFirstTimeLaunch = false
        verify { editor.putBoolean("is_first_time", false) }
        verify { editor.apply() }
    }

    @Test
    fun testIsLoggedIn_getAndSet() {
        every { prefs.getBoolean("is_logged_in", false) } returns false
        assertFalse(appPreferences.isLoggedIn)

        appPreferences.isLoggedIn = true
        verify { editor.putBoolean("is_logged_in", true) }
        verify { editor.apply() }
    }

    @Test
    fun testIsTestMode_getAndSet() {
        every { prefs.getBoolean("is_test_mode", false) } returns false
        assertFalse(appPreferences.isTestMode)

        appPreferences.isTestMode = true
        verify { editor.putBoolean("is_test_mode", true) }
        verify { editor.apply() }
    }
}
