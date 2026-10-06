package com.example.zarur.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.zarur.domain.model.CardBrand
import com.example.zarur.domain.model.PaymentMethod
import com.example.zarur.domain.model.PaymentType
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.repository.PaymentRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PaymentRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("zarur_payment_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val paymentMethodsState = MutableStateFlow<List<PaymentMethod>>(emptyList())

    init {
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val json = prefs.getString("saved_payment_methods", null)
        if (json.isNullOrEmpty()) {
            val initialList = createInitialDefaultMethods()
            saveToPrefs(initialList)
            paymentMethodsState.value = initialList
        } else {
            try {
                val type = object : TypeToken<List<PaymentMethod>>() {}.type
                val loaded: List<PaymentMethod> = gson.fromJson(json, type)
                paymentMethodsState.value = loaded.ifEmpty { createInitialDefaultMethods() }
            } catch (e: Exception) {
                val initialList = createInitialDefaultMethods()
                saveToPrefs(initialList)
                paymentMethodsState.value = initialList
            }
        }
    }

    private fun saveToPrefs(methods: List<PaymentMethod>) {
        val json = gson.toJson(methods)
        prefs.edit().putString("saved_payment_methods", json).apply()
        paymentMethodsState.value = methods
    }

    private fun createInitialDefaultMethods(): List<PaymentMethod> {
        return listOf(
            PaymentMethod(
                id = "card_1",
                type = PaymentType.CARD,
                brand = CardBrand.VISA,
                title = "Visa Premium",
                cardHolderName = "Andrew Ainsley",
                cardNumber = "4111222233334242",
                maskedNumber = "•••• •••• •••• 4242",
                expiryDate = "12/28",
                cvv = "888",
                isDefault = true
            ),
            PaymentMethod(
                id = "card_2",
                type = PaymentType.CARD,
                brand = CardBrand.MASTERCARD,
                title = "MasterCard Platinum",
                cardHolderName = "Andrew Ainsley",
                cardNumber = "5500888899998888",
                maskedNumber = "•••• •••• •••• 8888",
                expiryDate = "09/27",
                cvv = "321",
                isDefault = false
            ),
            PaymentMethod(
                id = "card_3",
                type = PaymentType.CARD,
                brand = CardBrand.AMEX,
                title = "American Express Gold",
                cardHolderName = "Andrew Ainsley",
                cardNumber = "378282246310005",
                maskedNumber = "•••• •••••• •1005",
                expiryDate = "05/29",
                cvv = "1234",
                isDefault = false
            ),
            PaymentMethod(
                id = "card_4",
                type = PaymentType.CARD,
                brand = CardBrand.UZCARD,
                title = "Uzcard Gold",
                cardHolderName = "Andrew Ainsley",
                cardNumber = "8600123456785678",
                maskedNumber = "•••• •••• •••• 5678",
                expiryDate = "11/26",
                cvv = "999",
                isDefault = false
            ),
            PaymentMethod(
                id = "wallet_paypal",
                type = PaymentType.DIGITAL_WALLET,
                brand = CardBrand.PAYPAL,
                title = "PayPal",
                cardHolderName = "andrew_ainsley@domain.com",
                isConnected = true,
                isDefault = false
            ),
            PaymentMethod(
                id = "wallet_gpay",
                type = PaymentType.DIGITAL_WALLET,
                brand = CardBrand.GOOGLE_PAY,
                title = "Google Pay",
                cardHolderName = "andrew.gpay@gmail.com",
                isConnected = true,
                isDefault = false
            ),
            PaymentMethod(
                id = "wallet_applepay",
                type = PaymentType.DIGITAL_WALLET,
                brand = CardBrand.APPLE_PAY,
                title = "Apple Pay",
                cardHolderName = "Connected",
                isConnected = true,
                isDefault = false
            ),
            PaymentMethod(
                id = "wallet_stripe",
                type = PaymentType.DIGITAL_WALLET,
                brand = CardBrand.STRIPE,
                title = "Stripe Express",
                cardHolderName = "Connected Merchant",
                isConnected = true,
                isDefault = false
            ),
            PaymentMethod(
                id = "wallet_klarna",
                type = PaymentType.DIGITAL_WALLET,
                brand = CardBrand.KLARNA,
                title = "Klarna Pay Later",
                cardHolderName = "Instant Credit",
                isConnected = true,
                isDefault = false
            ),
            PaymentMethod(
                id = "wallet_alipay",
                type = PaymentType.DIGITAL_WALLET,
                brand = CardBrand.ALIPAY,
                title = "Alipay Global",
                cardHolderName = "Connected Account",
                isConnected = true,
                isDefault = false
            ),
            PaymentMethod(
                id = "wallet_wechat",
                type = PaymentType.DIGITAL_WALLET,
                brand = CardBrand.WECHAT,
                title = "WeChat Pay",
                cardHolderName = "Connected Account",
                isConnected = true,
                isDefault = false
            )
        )
    }

    override fun getPaymentMethods(): Flow<List<PaymentMethod>> = paymentMethodsState

    override fun getSavedCards(): Flow<List<PaymentMethod>> =
        paymentMethodsState.map { list -> list.filter { it.type == PaymentType.CARD } }

    override fun getDigitalWallets(): Flow<List<PaymentMethod>> =
        paymentMethodsState.map { list -> list.filter { it.type == PaymentType.DIGITAL_WALLET } }

    override suspend fun addPaymentCard(
        cardHolderName: String,
        cardNumber: String,
        expiryDate: String,
        cvv: String,
        isDefault: Boolean
    ): Resource<PaymentMethod> {
        val cleanNumber = cardNumber.replace("\\s+".toRegex(), "")
        if (cleanNumber.length !in 15..16) {
            return Resource.Error("Card number must be 15 or 16 digits")
        }
        if (cardHolderName.isBlank()) {
            return Resource.Error("Cardholder name cannot be empty")
        }
        if (!expiryDate.matches("^\\d{2}/\\d{2}\$".toRegex())) {
            return Resource.Error("Expiry date must be in MM/YY format")
        }
        if (cvv.length !in 3..4) {
            return Resource.Error("CVV must be 3 or 4 digits")
        }

        val brand = CardBrand.detectFromNumber(cleanNumber)
        val masked = "•••• •••• •••• " + cleanNumber.takeLast(4)
        val newCardId = "card_" + UUID.randomUUID().toString().take(8)

        val currentList = paymentMethodsState.value.toMutableList()
        val formattedTitle = "${brand.brandName} Card"

        var updatedList = if (isDefault) {
            currentList.map {
                if (it.type == PaymentType.CARD) it.copy(isDefault = false) else it
            }.toMutableList()
        } else {
            currentList
        }

        val newCard = PaymentMethod(
            id = newCardId,
            type = PaymentType.CARD,
            brand = brand,
            title = formattedTitle,
            cardHolderName = cardHolderName.trim(),
            cardNumber = cleanNumber,
            maskedNumber = masked,
            expiryDate = expiryDate.trim(),
            cvv = cvv.trim(),
            isDefault = isDefault || updatedList.none { it.type == PaymentType.CARD && it.isDefault }
        )

        updatedList.add(0, newCard)
        saveToPrefs(updatedList)
        return Resource.Success(newCard)
    }

    override suspend fun deletePaymentMethod(id: String): Resource<Unit> {
        val currentList = paymentMethodsState.value.toMutableList()
        val itemToDelete = currentList.find { it.id == id } ?: return Resource.Error("Method not found")
        currentList.remove(itemToDelete)

        // If default card was removed, assign default to first remaining card
        if (itemToDelete.isDefault) {
            val firstCardIndex = currentList.indexOfFirst { it.type == PaymentType.CARD }
            if (firstCardIndex != -1) {
                currentList[firstCardIndex] = currentList[firstCardIndex].copy(isDefault = true)
            }
        }

        saveToPrefs(currentList)
        return Resource.Success(Unit)
    }

    override suspend fun setDefaultPaymentMethod(id: String): Resource<Unit> {
        val currentList = paymentMethodsState.value
        val target = currentList.find { it.id == id } ?: return Resource.Error("Method not found")

        val updatedList = currentList.map { item ->
            if (target.type == PaymentType.CARD) {
                if (item.type == PaymentType.CARD) {
                    item.copy(isDefault = item.id == id)
                } else item
            } else {
                item.copy(isDefault = item.id == id)
            }
        }

        saveToPrefs(updatedList)
        return Resource.Success(Unit)
    }

    override suspend fun toggleWalletConnection(id: String): Resource<Unit> {
        val updatedList = paymentMethodsState.value.map { item ->
            if (item.id == id) {
                item.copy(isConnected = !item.isConnected)
            } else item
        }
        saveToPrefs(updatedList)
        return Resource.Success(Unit)
    }

    override fun processPaymentSimulation(methodId: String, amount: Double): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        delay(1000) // Simulate merchant processing network request
        val method = paymentMethodsState.value.find { it.id == methodId }
        if (method != null) {
            val txnId = "TXN-" + System.currentTimeMillis().toString().takeLast(8)
            emit(Resource.Success(txnId))
        } else {
            emit(Resource.Error("Invalid payment method selected"))
        }
    }
}
