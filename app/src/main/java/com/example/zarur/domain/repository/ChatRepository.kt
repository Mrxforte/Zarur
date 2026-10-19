package com.example.zarur.domain.repository

import com.example.zarur.domain.model.CallLog
import com.example.zarur.domain.model.ChatConversation
import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getConversations(): Flow<List<ChatConversation>>
    fun getMessages(chatId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(chatId: String, message: ChatMessage)
    suspend fun getOrCreateChatForProduct(product: Product): ChatConversation

    fun getCallLogs(): Flow<List<CallLog>>
    suspend fun addCallLog(callLog: CallLog)
    suspend fun clearCallLogs()
}
