package com.example.zarur.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.zarur.domain.model.CallLog
import com.example.zarur.domain.model.CallStatus
import com.example.zarur.domain.model.CallType
import com.example.zarur.domain.model.ChatConversation
import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : ChatRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("zarur_chat_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val conversationsState = MutableStateFlow<List<ChatConversation>>(emptyList())
    private val callLogsState = MutableStateFlow<List<CallLog>>(emptyList())
    private val messagesCache = mutableMapOf<String, MutableList<ChatMessage>>()

    init {
        loadConversationsFromPrefs()
        loadCallLogsFromPrefs()
    }

    private fun loadConversationsFromPrefs() {
        val json = prefs.getString("chat_conversations", null)
        if (json.isNullOrEmpty()) {
            val initial = createDefaultConversations()
            saveConversationsToPrefs(initial)
        } else {
            try {
                val type = object : TypeToken<List<ChatConversation>>() {}.type
                val list: List<ChatConversation> = gson.fromJson(json, type)
                conversationsState.value = list.ifEmpty { createDefaultConversations() }
            } catch (e: Exception) {
                val initial = createDefaultConversations()
                saveConversationsToPrefs(initial)
            }
        }
    }

    private fun saveConversationsToPrefs(list: List<ChatConversation>) {
        val json = gson.toJson(list)
        prefs.edit().putString("chat_conversations", json).apply()
        conversationsState.value = list
    }

    private fun loadCallLogsFromPrefs() {
        val json = prefs.getString("chat_call_logs", null)
        if (json.isNullOrEmpty()) {
            val initial = createDefaultCallLogs()
            saveCallLogsToPrefs(initial)
        } else {
            try {
                val type = object : TypeToken<List<CallLog>>() {}.type
                val list: List<CallLog> = gson.fromJson(json, type)
                callLogsState.value = list.ifEmpty { createDefaultCallLogs() }
            } catch (e: Exception) {
                val initial = createDefaultCallLogs()
                saveCallLogsToPrefs(initial)
            }
        }
    }

    private fun saveCallLogsToPrefs(list: List<CallLog>) {
        val json = gson.toJson(list)
        prefs.edit().putString("chat_call_logs", json).apply()
        callLogsState.value = list
    }

    private fun createDefaultConversations(): List<ChatConversation> {
        return listOf(
            ChatConversation(
                chatId = "default_chat_id",
                productId = "1",
                productName = "La Grand Maison Villa",
                productImage = "",
                productPrice = 1200000.0,
                sellerId = "seller1",
                sellerName = "Natasya Wilodra",
                lastMessage = "E'lon bo'yicha uyni ko'rishga kelishingiz mumkin 😊",
                lastMessageTime = "16:00",
                unreadCount = 1,
                isSellerLive = true,
                allowVoiceCall = true,
                allowVideoCall = true
            ),
            ChatConversation(
                chatId = "chat_seller_charolette",
                productId = "2",
                productName = "iPhone 15 Pro Max",
                productImage = "",
                productPrice = 14500000.0,
                sellerId = "seller2",
                sellerName = "Charolette Hanlin",
                lastMessage = "Kafolat beriladi, holati ideal!",
                lastMessageTime = "14:45",
                unreadCount = 0,
                isSellerLive = false,
                allowVoiceCall = true,
                allowVideoCall = false
            )
        )
    }

    private fun createDefaultCallLogs(): List<CallLog> {
        return listOf(
            CallLog(
                id = "call_1",
                callerName = "Natasya Wilodra",
                callerAvatar = "",
                callType = CallType.VOICE,
                callStatus = CallStatus.DECLINED,
                time = "Today, 15:45",
                duration = null,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30
            ),
            CallLog(
                id = "call_2",
                callerName = "Charolette Hanlin",
                callerAvatar = "",
                callType = CallType.VIDEO,
                callStatus = CallStatus.OUTGOING,
                time = "Today, 14:20",
                duration = "3 min 24 sec",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120
            ),
            CallLog(
                id = "call_3",
                callerName = "Natasya Wilodra",
                callerAvatar = "",
                callType = CallType.VOICE,
                callStatus = CallStatus.INCOMING,
                time = "Yesterday, 18:10",
                duration = "1 min 45 sec",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24
            ),
            CallLog(
                id = "call_4",
                callerName = "Johnathan Doe",
                callerAvatar = "",
                callType = CallType.VOICE,
                callStatus = CallStatus.MISSED,
                time = "Yesterday, 11:05",
                duration = null,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 30
            ),
            CallLog(
                id = "call_5",
                callerName = "Charolette Hanlin",
                callerAvatar = "",
                callType = CallType.VIDEO,
                callStatus = CallStatus.DECLINED,
                time = "Dec 22, 09:15",
                duration = null,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 48
            ),
            CallLog(
                id = "call_6",
                callerName = "Official Real Estate",
                callerAvatar = "",
                callType = CallType.VOICE,
                callStatus = CallStatus.OUTGOING,
                time = "Dec 20, 16:30",
                duration = "5 min 10 sec",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 96
            )
        )
    }

    override fun getConversations(): Flow<List<ChatConversation>> = conversationsState

    override fun getCallLogs(): Flow<List<CallLog>> = callLogsState

    override suspend fun addCallLog(callLog: CallLog) {
        val updated = callLogsState.value.toMutableList().apply {
            add(0, callLog)
        }
        saveCallLogsToPrefs(updated)
    }

    override suspend fun clearCallLogs() {
        saveCallLogsToPrefs(emptyList())
    }

    override fun getMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null
        try {
            listenerRegistration = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(messagesCache[chatId] ?: emptyList())
                        return@addSnapshotListener
                    }
                    val messages = snapshot.documents.map { doc ->
                        ChatMessage(
                            id = doc.id,
                            chatId = chatId,
                            text = doc.getString("text") ?: "",
                            time = doc.getString("time") ?: "",
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            isSent = doc.getBoolean("isSent") ?: true,
                            isProperty = doc.getBoolean("isProperty") ?: false,
                            productId = doc.getString("productId") ?: "",
                            productName = doc.getString("productName") ?: "",
                            productPrice = doc.getString("productPrice") ?: "",
                            productImage = doc.getString("productImage") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    }
                    messagesCache[chatId] = messages.toMutableList()
                    trySend(messages)
                }
        } catch (e: Exception) {
            trySend(messagesCache[chatId] ?: emptyList())
        }
        awaitClose { listenerRegistration?.remove() }
    }

    override suspend fun sendMessage(chatId: String, message: ChatMessage) {
        val list = messagesCache.getOrPut(chatId) { mutableListOf() }
        list.add(message)

        // Update conversation last message
        val currentConvs = conversationsState.value.toMutableList()
        val index = currentConvs.indexOfFirst { it.chatId == chatId }
        if (index != -1) {
            val old = currentConvs[index]
            currentConvs[index] = old.copy(
                lastMessage = message.text,
                lastMessageTime = message.time,
                timestamp = message.timestamp
            )
            saveConversationsToPrefs(currentConvs)
        }

        val messageMap = mapOf(
            "text" to message.text,
            "time" to message.time,
            "senderId" to message.senderId,
            "senderName" to message.senderName,
            "isSent" to message.isSent,
            "isProperty" to message.isProperty,
            "productId" to message.productId,
            "productName" to message.productName,
            "productPrice" to message.productPrice,
            "productImage" to message.productImage,
            "timestamp" to message.timestamp
        )
        try {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageMap)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getOrCreateChatForProduct(product: Product): ChatConversation {
        val chatId = "chat_${product.id}"
        val existing = conversationsState.value.find { it.chatId == chatId }
        if (existing != null) return existing

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val newConv = ChatConversation(
            chatId = chatId,
            productId = product.id,
            productName = product.name,
            productImage = product.imageUrl,
            productPrice = product.price,
            sellerId = product.sellerId.ifEmpty { "seller_" + product.id },
            sellerName = product.sellerName.ifEmpty { "Official Store" },
            lastMessage = "Assalomu alaykum! '${product.name}' bo'yicha savolingiz bormi?",
            lastMessageTime = currentTime,
            unreadCount = 0,
            isSellerLive = product.isSellerLive,
            allowVoiceCall = product.allowVoiceCall,
            allowVideoCall = product.allowVideoCall
        )

        val updated = conversationsState.value.toMutableList().apply { add(0, newConv) }
        saveConversationsToPrefs(updated)

        // Seed product preview message
        val cardMsg = ChatMessage(
            id = "msg_prod_" + product.id,
            chatId = chatId,
            text = product.name,
            time = currentTime,
            isSent = false,
            isProperty = true,
            productId = product.id,
            productName = product.name,
            productPrice = "%,.0f uzs".format(product.price).replace(",", " "),
            productImage = product.imageUrl,
            timestamp = System.currentTimeMillis()
        )
        sendMessage(chatId, cardMsg)

        return newConv
    }
}
