package com.example.nimma_guru.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class Session(
    val sessionId: String = "",
    val mentorId: String = "",
    val mentorName: String = "",
    val title: String = "",
    val date: Timestamp? = null,
    val time: String = "",
    val venue: String = "",
    val attendees: Int = 0,
    val maxAttendees: Int = 20
) {
    fun formattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return date?.toDate()?.let { sdf.format(it) } ?: ""
    }

    fun isFull(): Boolean = attendees >= maxAttendees
}
