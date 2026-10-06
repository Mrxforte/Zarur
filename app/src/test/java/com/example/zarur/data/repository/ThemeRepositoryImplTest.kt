package com.example.zarur.data.repository

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ThemeRepositoryImplTest {

    private val context: Context = mockk()
    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    private lateinit var repository: ThemeRepositoryImpl

    @Before
    fun setUp() {
        every { context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor

        repository = ThemeRepositoryImpl(context)
    }

    @Test
    fun testIsDarkMode_default() = runTest {
        every { prefs.getBoolean("is_dark_mode", false) } returns false

        val isDark = repository.isDarkMode().first()
        assertFalse(isDark)
    }

    @Test
    fun testSetDarkMode() = runTest {
        repository.setDarkMode(true)

        verify { editor.putBoolean("is_dark_mode", true) }
        verify { editor.apply() }
    }
}
