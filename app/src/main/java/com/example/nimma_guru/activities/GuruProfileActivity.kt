package com.example.nimma_guru.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nimma_guru.R
import com.example.nimma_guru.databinding.ActivityGuruProfileBinding

class GuruProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuruProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuruProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Read extras
        val guruId = intent.getStringExtra("guruId") ?: ""
        val name = intent.getStringExtra("name") ?: "Guru Profile"
        val skills = intent.getStringExtra("skills") ?: ""
        val experience = intent.getStringExtra("experience") ?: "0"
        val availability = intent.getStringExtra("availability") ?: "Not set"
        val location = intent.getStringExtra("location") ?: "Unknown"
        val photoUrl = intent.getStringExtra("photoUrl") ?: ""
        val rating = intent.getFloatExtra("rating", 0f)
        val isAvailable = intent.getBooleanExtra("isAvailable", true)

        binding.toolbar.title = name
        binding.tvName.text = name
        binding.tvPrimarySkill.text = skills.split(",").firstOrNull()?.trim() ?: "Expert"

        binding.tvRating.text = String.format("%.1f ★", rating)
        binding.tvExperience.text = getString(R.string.years_experience, experience.toIntOrNull() ?: 0)

        if (isAvailable) {
            binding.tvStatus.text = getString(R.string.available)
            binding.tvStatus.setTextColor(getColor(R.color.status_available))
        } else {
            binding.tvStatus.text = getString(R.string.busy)
            binding.tvStatus.setTextColor(getColor(R.color.status_busy))
        }

        binding.tvLocation.text = location
        binding.tvAvailability.text = availability
        binding.tvSkillsList.text = skills

        if (photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivAvatar)
        }

        binding.btnConnect.setOnClickListener {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "Please log in to request a meeting", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (guruId.isEmpty()) {
                Toast.makeText(this, "Guru ID not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestData = hashMapOf(
                "studentId" to user.uid,
                "guruId" to guruId,
                "guruName" to name,
                "status" to "pending",
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("session_requests")
                .add(requestData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Session request sent to $name!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send request: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
