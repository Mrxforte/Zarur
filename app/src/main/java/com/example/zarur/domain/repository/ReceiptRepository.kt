package com.example.zarur.domain.repository

import com.example.zarur.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

interface ReceiptRepository {
    fun getReceipts(): Flow<List<Receipt>>
    suspend fun addReceipt(receipt: Receipt)
}
