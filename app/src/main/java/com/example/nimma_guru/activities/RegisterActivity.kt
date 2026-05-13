package com.example.nimma_guru.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nimma_guru.MainActivity
import com.example.nimma_guru.R
import com.example.nimma_guru.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private val TAG = "RegisterActivity"
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var role: String = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        role = intent.getStringExtra("role") ?: "student"

        binding.btnRegister.setOnClickListener { registerUser() }
        binding.tvAlreadyHaveAccount.setOnClickListener { finish() }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()       // no trim
        val confirmPassword = binding.etConfirmPassword.text.toString() // no trim

        // Validation
        if (name.isEmpty()) { binding.tilName.error = getString(R.string.error_name_required); return }
        binding.tilName.error = null
        if (email.isEmpty()) { binding.tilEmail.error = getString(R.string.error_email_required); return }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalid); return
        }
        binding.tilEmail.error = null
        if (phone.isEmpty()) { binding.tilPhone.error = getString(R.string.error_phone_required); return }
        binding.tilPhone.error = null
        if (password.length < 6) { binding.tilPassword.error = getString(R.string.error_password_short); return }
        binding.tilPassword.error = null
        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_no_match); return
        }
        binding.tilConfirmPassword.error = null

        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    // Send verification email
                    auth.currentUser?.sendEmailVerification()
                    saveUserToFirestore(name, email, phone)
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    val msg = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                        is FirebaseAuthUserCollisionException -> "Account already exists with this email"
                        else -> task.exception?.message ?: getString(R.string.error_generic)
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(name: String, email: String, phone: String) {
        val userId = auth.currentUser?.uid ?: return
        val userMap = hashMapOf(
            "userId" to userId,
            "role" to role,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "language" to "en",
            "photoUrl" to ""
        )
        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                // If guru, also create gurus doc
                if (role == "guru") {
                    val guruMap = hashMapOf(
                        "guruId" to userId,
                        "skills" to emptyList<String>(),
                        "experience" to "",
                        "availability" to "",
                        "location" to "",
                        "photoUrl" to "",
                        "rating" to 0f
                    )
                    firestore.collection("gurus").document(userId).set(guruMap)
                }
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Registered Successfully! Please verify your email.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
