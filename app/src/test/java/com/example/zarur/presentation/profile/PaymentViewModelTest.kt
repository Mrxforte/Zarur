package com.example.zarur.presentation.profile

import com.example.zarur.domain.model.CardBrand
import com.example.zarur.domain.model.PaymentMethod
import com.example.zarur.domain.model.PaymentType
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.repository.PaymentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    private val paymentRepository: PaymentRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: PaymentViewModel

    private val mockCard = PaymentMethod(
        id = "card_1",
        type = PaymentType.CARD,
        brand = CardBrand.VISA,
        title = "Visa Card",
        cardHolderName = "Andrew Ainsley",
        cardNumber = "4111222233334242",
        maskedNumber = "•••• •••• •••• 4242",
        expiryDate = "12/28",
        cvv = "123",
        isDefault = true
    )

    private val mockWallet = PaymentMethod(
        id = "wallet_paypal",
        type = PaymentType.DIGITAL_WALLET,
        brand = CardBrand.PAYPAL,
        title = "PayPal",
        cardHolderName = "andrew@domain.com",
        isConnected = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { paymentRepository.getPaymentMethods() } returns flowOf(listOf(mockCard, mockWallet))

        viewModel = PaymentViewModel(paymentRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads cards and digital wallets`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(1, state.cards.size)
        assertEquals(1, state.wallets.size)
        assertEquals("card_1", state.selectedMethodId)
    }

    @Test
    fun `addCard handles success resource`() = runTest {
        coEvery {
            paymentRepository.addPaymentCard(any(), any(), any(), any(), any())
        } returns Resource.Success(mockCard)

        viewModel.addCard("Andrew Ainsley", "4111222233334242", "12/28", "123", true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertNotNull(state.addCardSuccessMessage)
        assertEquals("card_1", state.selectedMethodId)
    }

    @Test
    fun `addCard handles error resource`() = runTest {
        coEvery {
            paymentRepository.addPaymentCard(any(), any(), any(), any(), any())
        } returns Resource.Error("Invalid card number")

        viewModel.addCard("Andrew Ainsley", "123", "12/28", "123", true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Invalid card number", state.error)
        assertNull(state.addCardSuccessMessage)
    }

    @Test
    fun `selectMethod updates selectedMethodId`() = runTest {
        advanceUntilIdle()
        viewModel.selectMethod("wallet_paypal")
        assertEquals("wallet_paypal", viewModel.uiState.value.selectedMethodId)
    }
}
