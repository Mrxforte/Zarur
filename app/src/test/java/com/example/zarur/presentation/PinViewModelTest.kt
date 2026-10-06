package com.example.zarur.presentation

import com.example.zarur.domain.usecase.IsPinSetUseCase
import com.example.zarur.domain.usecase.SavePinUseCase
import com.example.zarur.domain.usecase.ValidatePinUseCase
import com.example.zarur.presentation.common.PinViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PinViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val savePinUseCase: SavePinUseCase = mockk()
    private val validatePinUseCase: ValidatePinUseCase = mockk()
    private val isPinSetUseCase: IsPinSetUseCase = mockk()

    private lateinit var viewModel: PinViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { savePinUseCase(any()) } returns Unit
        coEvery { validatePinUseCase(any()) } returns true
        viewModel = PinViewModel(savePinUseCase, validatePinUseCase, isPinSetUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testOnNumberClick_andDelete() {
        viewModel.onNumberClick("1")
        viewModel.onNumberClick("2")
        viewModel.onNumberClick("3")
        viewModel.onNumberClick("4")
        viewModel.onNumberClick("5") // Should be ignored as max length is 4

        assertEquals("1234", viewModel.pinState.value)

        viewModel.onDeleteClick()
        assertEquals("123", viewModel.pinState.value)
    }

    @Test
    fun testSavePin() = runTest {
        coEvery { savePinUseCase("1234") } returns Unit

        viewModel.onNumberClick("1")
        viewModel.onNumberClick("2")
        viewModel.onNumberClick("3")
        viewModel.onNumberClick("4")

        viewModel.savePin()

        coVerify { savePinUseCase("1234") }
    }

    @Test
    fun testValidatePin_success() = runTest {
        coEvery { savePinUseCase("1234") } returns Unit

        viewModel.onNumberClick("1")
        viewModel.onNumberClick("2")
        viewModel.onNumberClick("3")
        viewModel.onNumberClick("4")

        viewModel.validatePin()

        coVerify { savePinUseCase("1234") }
    }

    @Test
    fun testValidatePin_dummy() = runTest {
        coEvery { savePinUseCase("0000") } returns Unit

        viewModel.onNumberClick("0")
        viewModel.onNumberClick("0")
        viewModel.onNumberClick("0")
        viewModel.onNumberClick("0")

        viewModel.validatePin()

        assertEquals("0000", viewModel.pinState.value)
    }
}
