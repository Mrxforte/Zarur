package com.example.zarur.domain.repository

import com.example.zarur.domain.model.PaymentMethod
import com.example.zarur.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getPaymentMethods(): Flow<List<PaymentMethod>>
    fun getSavedCards(): Flow<List<PaymentMethod>>
    fun getDigitalWallets(): Flow<List<PaymentMethod>>
    suspend fun addPaymentCard(
        cardHolderName: String,
        cardNumber: String,
        expiryDate: String,
        cvv: String,
        isDefault: Boolean
    ): Resource<PaymentMethod>
    suspend fun deletePaymentMethod(id: String): Resource<Unit>
    suspend fun setDefaultPaymentMethod(id: String): Resource<Unit>
    suspend fun toggleWalletConnection(id: String): Resource<Unit>
    fun processPaymentSimulation(methodId: String, amount: Double): Flow<Resource<String>>
}
