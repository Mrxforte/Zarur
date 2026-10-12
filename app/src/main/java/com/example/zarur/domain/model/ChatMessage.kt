package com.example.zarur.domain.model

data class ChatMessage(
    val id: String = "",
    val chatId: String = "default_chat_id",
    val text: String = "",
    val time: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val isSent: Boolean = true,
    val isProperty: Boolean = false,
    val productId: String = "",
    val productName: String = "",
    val productPrice: String = "",
    val productImage: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
