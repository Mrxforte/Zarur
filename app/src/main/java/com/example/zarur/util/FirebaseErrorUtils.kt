package com.example.zarur.util

fun parseFirebaseError(e: Throwable): String {
    val message = e.message ?: ""
    val localizedMsg = e.localizedMessage ?: ""
    val fullErr = "$e $message $localizedMsg"

    return when {
        fullErr.contains("PERMISSION_DENIED", ignoreCase = true) ||
        fullErr.contains("permission-denied", ignoreCase = true) ||
        fullErr.contains("insufficient permissions", ignoreCase = true) ||
        fullErr.contains("Missing or insufficient permissions", ignoreCase = true) -> {
            "Ruxsat rad etildi. Ushbu amalni bajarish uchun avval tizimga kiring."
        }

        fullErr.contains("UNAUTHENTICATED", ignoreCase = true) ||
        fullErr.contains("unauthenticated", ignoreCase = true) -> {
            "Tizimga kirilmagan. Iltimos, qaytadan tizimga kiring."
        }

        fullErr.contains("NOT_FOUND", ignoreCase = true) ||
        fullErr.contains("No document to update", ignoreCase = true) -> {
            "Ma'lumot topilmadi yoki o'chirilgan."
        }

        fullErr.contains("network", ignoreCase = true) ||
        fullErr.contains("connection", ignoreCase = true) ||
        fullErr.contains("UNAVAILABLE", ignoreCase = true) -> {
            "Internet ulanishida xatolik yuz berdi. Tarmoqni tekshirib qayta urinib ko'ring."
        }

        else -> {
            if (localizedMsg.contains("com.google.firebase") || localizedMsg.contains("Exception") || localizedMsg.contains("projects/")) {
                "Xatolik yuz berdi. Iltimos, qaytadan urinib ko'ring."
            } else if (localizedMsg.isNotBlank()) {
                localizedMsg
            } else {
                "Xatolik yuz berdi. Iltimos, qaytadan urinib ko'ring."
            }
        }
    }
}
