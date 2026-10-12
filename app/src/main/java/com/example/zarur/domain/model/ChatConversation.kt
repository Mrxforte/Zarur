package com.example.zarur.domain.model

data class ChatConversation(
    val chatId: String,
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val productPrice: Double = 0.0,
    val sellerId: String = "",
    val sellerName: String = "",
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val unreadCount: Int = 0,
    val isSellerLive: Boolean = false,
    val allowVoiceCall: Boolean = true,
    val allowVideoCall: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
