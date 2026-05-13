package com.example.nimma_guru.model

import com.google.firebase.Timestamp

data class Appreciation(
    val postId: String = "",
    val guruId: String = "",
    val guruName: String = "",
    val guruSubject: String = "",
    val studentId: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null,
    val count: Int = 0   // used for Wall of Fame aggregation
)
