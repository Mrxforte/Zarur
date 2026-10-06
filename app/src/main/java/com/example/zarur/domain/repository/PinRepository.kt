package com.example.zarur.domain.repository

interface PinRepository {
    suspend fun savePin(pin: String)
    suspend fun validatePin(pin: String): Boolean
    suspend fun isPinSet(): Boolean
}
