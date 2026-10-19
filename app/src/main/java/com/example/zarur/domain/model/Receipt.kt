package com.example.zarur.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Receipt(
    val id: String,
    val transactionId: String,
    val productName: String,
    val productImage: String = "",
    val priceFormatted: String,
    val category: String = "Real Estate",
    val date: String,
    val status: String = "PAID",
    val paymentMethod: String = "Uzcard / Humo",
    val userName: String = "",
    val userPhone: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
