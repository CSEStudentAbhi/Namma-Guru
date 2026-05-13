package com.example.nimma_guru.model

data class Guru(
    val guruId: String = "",
    val name: String = "",
    val skills: List<String> = emptyList(),
    val experience: String = "",
    val availability: String = "",
    val location: String = "",
    val photoUrl: String = "",
    val rating: Float = 0f,
    val distanceKm: Float = 0f,
    val isAvailable: Boolean = true
) {
    val primarySkill: String get() = skills.firstOrNull() ?: ""
}
