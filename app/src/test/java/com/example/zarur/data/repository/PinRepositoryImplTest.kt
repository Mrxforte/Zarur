package com.example.zarur.data.repository

import android.content.SharedPreferences
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PinRepositoryImplTest {

    private val securePrefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    private lateinit var repository: PinRepositoryImpl

    @Before
    fun setUp() {
        every { securePrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor

        repository = PinRepositoryImpl(securePrefs)
    }

    @Test
    fun testSavePin() = runTest {
        repository.savePin("1234")

        verify { editor.putString("user_pin", "1234") }
        verify { editor.apply() }
    }

    @Test
    fun testValidatePin() = runTest {
        every { securePrefs.getString("user_pin", null) } returns "1234"

        assertTrue(repository.validatePin("1234"))
        assertFalse(repository.validatePin("0000"))
    }

    @Test
    fun testIsPinSet() = runTest {
        every { securePrefs.contains("user_pin") } returns true
        assertTrue(repository.isPinSet())

        every { securePrefs.contains("user_pin") } returns false
        assertFalse(repository.isPinSet())
    }
}
