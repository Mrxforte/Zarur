package com.example.zarur.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.zarur.domain.model.Receipt
import com.example.zarur.domain.repository.ReceiptRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ReceiptRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("zarur_receipts_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val receiptsState = MutableStateFlow<List<Receipt>>(emptyList())

    init {
        loadReceipts()
    }

    private fun loadReceipts() {
        val json = prefs.getString("receipts_list", null)
        if (json.isNullOrEmpty()) {
            val initial = createDefaultReceipts()
            saveReceipts(initial)
        } else {
            try {
                val type = object : TypeToken<List<Receipt>>() {}.type
                val list: List<Receipt> = gson.fromJson(json, type)
                receiptsState.value = list.ifEmpty { createDefaultReceipts() }
            } catch (e: Exception) {
                val initial = createDefaultReceipts()
                saveReceipts(initial)
            }
        }
    }

    private fun saveReceipts(list: List<Receipt>) {
        val json = gson.toJson(list)
        prefs.edit().putString("receipts_list", json).apply()
        receiptsState.value = list
    }

    override fun getReceipts(): Flow<List<Receipt>> = receiptsState

    override suspend fun addReceipt(receipt: Receipt) {
        val updated = receiptsState.value.toMutableList().apply {
            add(0, receipt)
        }
        saveReceipts(updated)
    }

    private fun createDefaultReceipts(): List<Receipt> {
        return listOf(
            Receipt(
                id = "rcpt_1",
                transactionId = "5457383979",
                productName = "La Grand Maison Villa",
                productImage = "",
                priceFormatted = "1 200 000 uzs",
                category = "Real Estate",
                date = "24 Dec 2024, 15:30",
                status = "PAID",
                paymentMethod = "Uzcard / Humo"
            ),
            Receipt(
                id = "rcpt_2",
                transactionId = "8821940212",
                productName = "iPhone 15 Pro Max 256GB",
                productImage = "",
                priceFormatted = "14 500 000 uzs",
                category = "Electronics",
                date = "22 Dec 2024, 11:15",
                status = "PAID",
                paymentMethod = "Visa Card (*4821)"
            ),
            Receipt(
                id = "rcpt_3",
                transactionId = "3910294810",
                productName = "Toyota Camry 2023 Executive",
                productImage = "",
                priceFormatted = "280 000 000 uzs",
                category = "Auto",
                date = "18 Dec 2024, 09:45",
                status = "PAID",
                paymentMethod = "Humo Card (*9012)"
            )
        )
    }
}
