package com.example.nimma_guru.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nimma_guru.databinding.ActivityHostMeetingBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class HostMeetingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHostMeetingBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHostMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnHost.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val dateStr = binding.etDate.text.toString().trim()
            val time = binding.etTime.text.toString().trim()
            val venue = binding.etVenue.text.toString().trim()
            val maxAttendeesStr = binding.etMaxAttendees.text.toString().trim()

            if (title.isEmpty() || dateStr.isEmpty() || time.isEmpty() || venue.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val maxAttendees = maxAttendeesStr.toIntOrNull() ?: 20
            
            // Parse date
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            var dateObj: Date? = null
            try {
                dateObj = sdf.parse(dateStr)
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timestamp = if (dateObj != null) Timestamp(dateObj) else Timestamp.now()
            
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get user details to get mentor name
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val mentorName = doc.getString("name") ?: "Unknown Guru"
                    
                    val sessionData = hashMapOf(
                        "mentorId" to user.uid,
                        "mentorName" to mentorName,
                        "title" to title,
                        "date" to timestamp,
                        "time" to time,
                        "venue" to venue,
                        "attendees" to 0,
                        "maxAttendees" to maxAttendees
                    )

                    firestore.collection("sessions").add(sessionData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Meeting hosted successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to host meeting: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get user info: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
