package com.example.nimma_guru.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nimma_guru.MainActivity
import com.example.nimma_guru.R
import com.example.nimma_guru.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLogin.setOnClickListener { loginUser() }
        binding.btnGoogleSignIn.setOnClickListener { signInWithGoogle() }
        binding.tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }
        binding.tvRegisterNow.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnContinueStudent.setOnClickListener { navigateAsGuest("student") }
        binding.btnContinueGuru.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java).apply {
                putExtra("role", "guru")
            })
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGoogleSignIn.isEnabled = false
        
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                binding.btnGoogleSignIn.isEnabled = true
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    checkUserRegistration(user?.uid)
                } else {
                    Toast.makeText(this, "Firebase Auth failed: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserRegistration(uid: String?) {
        if (uid == null) return
        
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    goToMain()
                } else {
                    // If user doesn't exist in Firestore, save basic info
                    val user = auth.currentUser
                    val userMap = hashMapOf(
                        "name" to (user?.displayName ?: ""),
                        "email" to (user?.email ?: ""),
                        "userId" to uid,
                        "role" to "student" // default role
                    )
                    firestore.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener { goToMain() }
                        .addOnFailureListener { goToMain() } // Still go to main but log error
                }
            }
            .addOnFailureListener {
                goToMain()
            }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString() // no trim on password

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required); return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalid); return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required); return
        }
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: getString(R.string.error_generic),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showForgotPasswordDialog() {
        val email = binding.etEmail.text.toString().trim()
        val inputEmail = android.widget.EditText(this).apply {
            hint = getString(R.string.email)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            if (email.isNotEmpty()) setText(email)
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.forgot_password_title))
            .setMessage(getString(R.string.forgot_password_message))
            .setView(inputEmail)
            .setPositiveButton(getString(R.string.send_reset_link)) { _, _ ->
                val resetEmail = inputEmail.text.toString().trim()
                if (resetEmail.isNotEmpty()) {
                    auth.sendPasswordResetEmail(resetEmail)
                        .addOnSuccessListener {
                            Toast.makeText(this, getString(R.string.reset_email_sent), Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun navigateAsGuest(role: String) {
        // For guest / demo mode — skip auth, go to MainActivity
        goToMain()
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
