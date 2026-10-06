package com.example.zarur.data.repository

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LanguageRepositoryImplTest {

    private val context: Context = mockk()
    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    private lateinit var repository: LanguageRepositoryImpl

    @Before
    fun setUp() {
        every { context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor

        repository = LanguageRepositoryImpl(context)
    }

    @Test
    fun testGetLanguage_default() = runTest {
        every { prefs.getString("selected_language", "ru") } returns "ru"

        val lang = repository.getLanguage().first()
        assertEquals("ru", lang)
    }

    @Test
    fun testSetLanguage() = runTest {
        repository.setLanguage("uz")

        verify { editor.putString("selected_language", "uz") }
        verify { editor.apply() }
    }
}
