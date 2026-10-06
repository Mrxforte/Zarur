package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, message: ChatMessage) {
        chatRepository.sendMessage(chatId, message)
    }
}
