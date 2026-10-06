package com.example.zarur.presentation.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.usecase.GetMessagesUseCase
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val chatId = "default_chat_id"

    init {
        loadMessages()
    }

    fun isLoggedIn(): Boolean = isLoggedInUseCase()

    private fun loadMessages() {
        viewModelScope.launch {
            getMessagesUseCase(chatId).collect { messageList ->
                if (messageList.isEmpty()) {
                    val initial = listOf(
                        ChatMessage(text = "PROPERTY", time = "", isSent = false, isProperty = true, timestamp = 1L),
                        ChatMessage(text = "I want to book your apartment for 5 days. Can it?", time = "16:00", isSent = true, timestamp = 2L),
                        ChatMessage(text = "Hello, good afternoon too Andrew..", time = "16:01", isSent = false, timestamp = 3L),
                        ChatMessage(text = "Of course, the apartment is always open anytime", time = "16:01", isSent = false, timestamp = 4L),
                        ChatMessage(text = "Great! I will wait for your booking and arrival!", time = "16:03", isSent = true, timestamp = 5L)
                    )
                    initial.forEach { sendMessageUseCase(chatId, it) }
                } else {
                    _messages.value = messageList
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        val message = ChatMessage(
            text = text,
            time = currentTime,
            isSent = true,
            isProperty = false,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            try {
                sendMessageUseCase(chatId, message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
