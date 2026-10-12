package com.example.zarur.presentation.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.ChatConversation
import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.repository.ChatRepository
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var activeChatId: String = "default_chat_id"

    init {
        loadConversations()
        loadMessages(activeChatId)
    }

    fun isLoggedIn(): Boolean = isLoggedInUseCase()

    fun loadConversations() {
        viewModelScope.launch {
            chatRepository.getConversations().collect { list ->
                _conversations.value = list
            }
        }
    }

    fun loadMessages(chatId: String) {
        activeChatId = chatId
        viewModelScope.launch {
            chatRepository.getMessages(chatId).collect { messageList ->
                _messages.value = messageList
            }
        }
    }

    fun openChatForProduct(product: Product, onChatReady: (ChatConversation) -> Unit) {
        viewModelScope.launch {
            val conv = chatRepository.getOrCreateChatForProduct(product)
            activeChatId = conv.chatId
            loadMessages(conv.chatId)
            onChatReady(conv)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            chatId = activeChatId,
            text = text.trim(),
            time = currentTime,
            isSent = true,
            isProperty = false,
            timestamp = System.currentTimeMillis()
        )

        val updated = _messages.value.toMutableList().apply { add(userMsg) }
        _messages.value = updated

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(activeChatId, userMsg)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            delay(1500)
            val replyText = getAutoReplyText(text)
            val replyTime = timeFormat.format(Date())
            val replyMsg = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                chatId = activeChatId,
                text = replyText,
                time = replyTime,
                isSent = false,
                isProperty = false,
                timestamp = System.currentTimeMillis()
            )

            val withReply = _messages.value.toMutableList().apply { add(replyMsg) }
            _messages.value = withReply
            try {
                chatRepository.sendMessage(activeChatId, replyMsg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAutoReplyText(userInput: String): String {
        val lower = userInput.lowercase()
        return when {
            lower.contains("narx") || lower.contains("narxi") || lower.contains("qancha") ->
                "Sotuvchi narx bo'yicha kelishishi mumkin. Keling, joyida uchrashib gaplashamiz 👍"
            lower.contains("manzil") || lower.contains("qayerda") || lower.contains("lokatsiya") ->
                "Manzil e'lon sahifasida ko'rsatilgan. Toshkent shahri markaziga yaqin joylashgan."
            lower.contains("telefon") || lower.contains("nomer") || lower.contains("aloqa") ->
                "Telefon raqamim: +998 90 123 45 67. Bemalol qo'ng'iroq qilishingiz mumkin!"
            else ->
                "Rahmat! Xabaringiz qabul qilindi. Tez orada batafsil javob beraman 😊"
        }
    }
}
