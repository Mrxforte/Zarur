package com.example.zarur.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.zarur.domain.model.CardBrand
import com.example.zarur.domain.model.PaymentType
import com.example.zarur.domain.model.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PaymentRepositoryImplTest {

    private val context: Context = mockk(relaxed = true)
    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)

    private lateinit var repository: PaymentRepositoryImpl

    @Before
    fun setup() {
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.getString(any(), any()) } returns null
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor

        repository = PaymentRepositoryImpl(context)
    }

    @Test
    fun `getPaymentMethods returns initial list when prefs empty`() = runTest {
        val methods = repository.getPaymentMethods().first()
        assertTrue(methods.isNotEmpty())
        assertTrue(methods.any { it.brand == CardBrand.VISA })
        assertTrue(methods.any { it.brand == CardBrand.MASTERCARD })
    }

    @Test
    fun `addPaymentCard successfully adds new Visa card and formats number`() = runTest {
        val result = repository.addPaymentCard(
            cardHolderName = "John Doe",
            cardNumber = "4111 2222 3333 4444",
            expiryDate = "10/30",
            cvv = "123",
            isDefault = true
        )

        assertTrue(result is Resource.Success)
        val addedCard = (result as Resource.Success).data
        assertEquals("John Doe", addedCard.cardHolderName)
        assertEquals(CardBrand.VISA, addedCard.brand)
        assertEquals("•••• •••• •••• 4444", addedCard.maskedNumber)
        assertTrue(addedCard.isDefault)

        val allMethods = repository.getPaymentMethods().first()
        val firstCard = allMethods.first { it.type == PaymentType.CARD }
        assertEquals(addedCard.id, firstCard.id)
    }

    @Test
    fun `addPaymentCard fails with invalid card number length`() = runTest {
        val result = repository.addPaymentCard(
            cardHolderName = "John Doe",
            cardNumber = "1234",
            expiryDate = "10/30",
            cvv = "123",
            isDefault = false
        )

        assertTrue(result is Resource.Error)
        assertEquals("Card number must be 15 or 16 digits", (result as Resource.Error).message)
    }

    @Test
    fun `deletePaymentMethod removes method successfully`() = runTest {
        val initialMethods = repository.getPaymentMethods().first()
        val cardToDelete = initialMethods.first { it.type == PaymentType.CARD }

        val deleteResult = repository.deletePaymentMethod(cardToDelete.id)
        assertTrue(deleteResult is Resource.Success)

        val updatedMethods = repository.getPaymentMethods().first()
        assertTrue(updatedMethods.none { it.id == cardToDelete.id })
    }

    @Test
    fun `processPaymentSimulation returns success transaction ID`() = runTest {
        val initialMethods = repository.getPaymentMethods().first()
        val firstMethod = initialMethods.first()

        val results = mutableListOf<Resource<String>>()
        repository.processPaymentSimulation(firstMethod.id, 99.99).collect {
            results.add(it)
        }

        assertTrue(results.first() is Resource.Loading)
        assertTrue(results.last() is Resource.Success)
        val txnId = (results.last() as Resource.Success).data
        assertTrue(txnId.startsWith("TXN-"))
    }
}
