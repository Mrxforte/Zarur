package com.example.zarur.app

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class LocaleManagerTest {

    @Test
    fun testUpdateBaseContextLocale() {
        val context: Context = mockk()
        val resources: Resources = mockk()
        val configuration = Configuration()
        val newContext: Context = mockk()

        every { context.resources } returns resources
        every { resources.configuration } returns configuration
        every { context.createConfigurationContext(any()) } returns newContext

        val result = LocaleManager.updateBaseContextLocale(context, "uz")

        assertEquals(newContext, result)
        assertEquals(Locale("uz"), Locale.getDefault())
    }

    @Test
    fun testApplyLocale() {
        val context: Context = mockk()
        val resources: Resources = mockk(relaxed = true)
        val configuration = Configuration()

        every { context.resources } returns resources
        every { resources.configuration } returns configuration

        LocaleManager.applyLocale(context, "ru")

        verify { resources.updateConfiguration(any(), any()) }
        assertEquals(Locale("ru"), Locale.getDefault())
    }
}
