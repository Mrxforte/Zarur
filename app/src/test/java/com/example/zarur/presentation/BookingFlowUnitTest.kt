package com.example.zarur.presentation

import com.example.zarur.domain.model.PaymentMethod
import com.example.zarur.domain.model.PaymentType
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.repository.PaymentRepository
import com.example.zarur.domain.usecase.AddReviewUseCase
import com.example.zarur.domain.usecase.IsPinSetUseCase
import com.example.zarur.domain.usecase.SavePinUseCase
import com.example.zarur.domain.usecase.ValidatePinUseCase
import com.example.zarur.presentation.booking.LeaveReviewViewModel
import com.example.zarur.presentation.common.PinViewModel
import com.example.zarur.presentation.common.UiState
import com.example.zarur.presentation.profile.PaymentViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingFlowUnitTest {

    private val testDispatcher = StandardTestDispatcher()

    private val savePinUseCase: SavePinUseCase = mockk(relaxed = true)
    private val validatePinUseCase: ValidatePinUseCase = mockk(relaxed = true)
    private val isPinSetUseCase: IsPinSetUseCase = mockk(relaxed = true)
    private val paymentRepository: PaymentRepository = mockk(relaxed = true)
    private val addReviewUseCase: AddReviewUseCase = mockk(relaxed = true)

    private lateinit var pinViewModel: PinViewModel
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var leaveReviewViewModel: LeaveReviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { validatePinUseCase(any()) } returns true
        coEvery { isPinSetUseCase() } returns true
        every { paymentRepository.getPaymentMethods() } returns flowOf(
            listOf(
                PaymentMethod(id = "card_1", title = "Visa", type = PaymentType.CARD, isDefault = true)
            )
        )

        pinViewModel = PinViewModel(savePinUseCase, validatePinUseCase, isPinSetUseCase)
        paymentViewModel = PaymentViewModel(paymentRepository)
        leaveReviewViewModel = LeaveReviewViewModel(addReviewUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testBookingPinValidation() = runTest {
        pinViewModel.onNumberClick("1")
        pinViewModel.onNumberClick("2")
        pinViewModel.onNumberClick("3")
        pinViewModel.onNumberClick("4")

        assertEquals("1234", pinViewModel.pinState.value)

        var successEmitted = false
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            pinViewModel.successEvent.collect {
                successEmitted = true
            }
        }

        pinViewModel.validatePin()
        advanceUntilIdle()

        assertTrue(successEmitted)
    }

    @Test
    fun testBookingPaymentSelection() = runTest {
        advanceUntilIdle()
        val cards = paymentViewModel.uiState.value.cards
        assertTrue(cards.isNotEmpty())
        assertEquals("card_1", paymentViewModel.uiState.value.selectedMethodId)

        paymentViewModel.selectMethod("card_2")
        assertEquals("card_2", paymentViewModel.uiState.value.selectedMethodId)
    }

    @Test
    fun testBookingSubmitReview() = runTest {
        coEvery { addReviewUseCase("prod_1", 5.0f, "Great stay!") } returns flowOf(Resource.Success(Unit))

        leaveReviewViewModel.submitReview("prod_1", 5.0f, "Great stay!")
        advanceUntilIdle()

        assertTrue(leaveReviewViewModel.submitState.value is UiState.Success)
    }
}
