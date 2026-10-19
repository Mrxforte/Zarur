package com.example.zarur.domain.model

enum class BookingStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

data class BookingItem(
    val id: String = "",
    val propertyId: String = "",
    val propertyName: String = "",
    val propertyImage: String = "",
    val location: String = "",
    val price: Double = 0.0,
    val formattedPrice: String = "",
    val dates: String = "",
    val bookingCode: String = "",
    val status: BookingStatus = BookingStatus.ACTIVE,
    val timestamp: Long = System.currentTimeMillis()
)
