package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(chatId: String): Flow<List<ChatMessage>> {
        return chatRepository.getMessages(chatId)
    }
}
