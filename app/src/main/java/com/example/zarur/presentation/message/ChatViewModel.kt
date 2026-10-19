package com.example.zarur.presentation.message

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.R
import com.example.zarur.domain.model.CallLog
import com.example.zarur.domain.model.CallStatus
import com.example.zarur.domain.model.CallType
import com.example.zarur.domain.model.ChatConversation
import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.repository.ChatRepository
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val chatRepository: ChatRepository,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var activeChatId: String = "default_chat_id"

    init {
        loadConversations()
        loadCallLogs()
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

    fun loadCallLogs() {
        viewModelScope.launch {
            chatRepository.getCallLogs().collect { list ->
                _callLogs.value = list
            }
        }
    }

    fun recordCallLog(
        callerName: String,
        callType: CallType,
        callStatus: CallStatus,
        durationSeconds: Long = 0
    ) {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = "Today, " + timeFormat.format(Date())

            val formattedDuration = when {
                callStatus == CallStatus.DECLINED || callStatus == CallStatus.MISSED -> null
                durationSeconds <= 0 -> "0 sec"
                durationSeconds < 60 -> "$durationSeconds sec"
                else -> {
                    val mins = durationSeconds / 60
                    val secs = durationSeconds % 60
                    if (secs > 0) "$mins min $secs sec" else "$mins min"
                }
            }

            val newLog = CallLog(
                id = System.currentTimeMillis().toString(),
                callerName = callerName.ifBlank { "Natasya Wilodra" },
                callerAvatar = "",
                callType = callType,
                callStatus = callStatus,
                time = formattedTime,
                duration = formattedDuration,
                timestamp = System.currentTimeMillis()
            )

            chatRepository.addCallLog(newLog)
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
            lower.contains("narx") || lower.contains("narxi") || lower.contains("qancha") || lower.contains("price") ->
                context.getString(R.string.chat_reply_price)
            lower.contains("manzil") || lower.contains("qayerda") || lower.contains("lokatsiya") || lower.contains("address") ->
                context.getString(R.string.chat_reply_location)
            lower.contains("telefon") || lower.contains("nomer") || lower.contains("aloqa") || lower.contains("phone") ->
                context.getString(R.string.chat_reply_phone)
            else ->
                context.getString(R.string.chat_reply_default)
        }
    }
}
