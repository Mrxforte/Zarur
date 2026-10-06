package com.example.zarur.domain.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val nickname: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val dob: String = "",
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
