package com.example.zarur.data.repository

import com.example.zarur.domain.model.ChatMessage
import com.example.zarur.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun getMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listenerRegistration = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.documents.map { doc ->
                        ChatMessage(
                            id = doc.id,
                            text = doc.getString("text") ?: "",
                            time = doc.getString("time") ?: "",
                            senderId = doc.getString("senderId") ?: "",
                            isSent = doc.getBoolean("isSent") ?: true,
                            isProperty = doc.getBoolean("isProperty") ?: false,
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    }
                    trySend(messages)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun sendMessage(chatId: String, message: ChatMessage) {
        val messageMap = mapOf(
            "text" to message.text,
            "time" to message.time,
            "senderId" to message.senderId,
            "isSent" to message.isSent,
            "isProperty" to message.isProperty,
            "timestamp" to message.timestamp
        )
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(messageMap)
            .await()
    }
}
