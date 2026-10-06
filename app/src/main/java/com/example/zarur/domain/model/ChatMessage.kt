package com.example.zarur.domain.model

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val time: String = "",
    val senderId: String = "",
    val isSent: Boolean = true,
    val isProperty: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
