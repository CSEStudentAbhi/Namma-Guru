package com.example.nimma_guru.model

data class User(
    val userId: String = "",
    val role: String = "student",   // "student" or "guru"
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val language: String = "en",
    val photoUrl: String = ""
)
