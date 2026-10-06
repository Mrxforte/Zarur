package com.example.zarur.domain.model

data class Review(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val userName: String = "Anonymous",
    val userAvatarUrl: String? = null,
    val rating: Float = 5.0f,
    val reviewText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
