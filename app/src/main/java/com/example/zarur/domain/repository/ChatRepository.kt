package com.example.zarur.domain.repository

import com.example.zarur.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(chatId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(chatId: String, message: ChatMessage)
}
