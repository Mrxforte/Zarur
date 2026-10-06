package com.example.zarur.presentation

import com.example.zarur.domain.usecase.GetLanguageUseCase
import com.example.zarur.domain.usecase.SetLanguageUseCase
import com.example.zarur.presentation.profile.LanguageViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanguageViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getLanguageUseCase: GetLanguageUseCase = mockk()
    private val setLanguageUseCase: SetLanguageUseCase = mockk()

    private lateinit var viewModel: LanguageViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getLanguageUseCase() } returns flowOf("uz")
        coEvery { setLanguageUseCase("en") } returns Unit

        viewModel = LanguageViewModel(getLanguageUseCase, setLanguageUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSelectedLanguage() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.selectedLanguage.collect()
        }
        assertEquals("uz", viewModel.selectedLanguage.value)
    }

    @Test
    fun testSelectLanguage() = runTest {
        viewModel.selectLanguage("en")
        coVerify { setLanguageUseCase("en") }
    }
}
