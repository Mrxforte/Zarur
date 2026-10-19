package com.example.zarur.domain.model

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    INCOMING,
    OUTGOING,
    MISSED,
    DECLINED
}

data class CallLog(
    val id: String = "",
    val callerName: String = "",
    val callerAvatar: String = "",
    val callType: CallType = CallType.VOICE,
    val callStatus: CallStatus = CallStatus.INCOMING,
    val time: String = "",
    val duration: String? = null, // e.g. "2 min 15 sec", "45 sec", "5 min", or null if declined/missed
    val timestamp: Long = System.currentTimeMillis()
)
