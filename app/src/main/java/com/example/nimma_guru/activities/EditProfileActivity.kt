package com.example.nimma_guru.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nimma_guru.R
import com.example.nimma_guru.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.fabChangePhoto.setOnClickListener { openImagePicker() }
        binding.tvChangePhoto.setOnClickListener { openImagePicker() }
        binding.btnSave.setOnClickListener { saveProfile() }
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                binding.etName.setText(doc.getString("name") ?: "")
                binding.etPhone.setText(doc.getString("phone") ?: "")
                val photo = doc.getString("photoUrl") ?: ""
                if (photo.isNotEmpty()) {
                    Glide.with(this).load(photo).circleCrop().into(binding.ivProfilePhoto)
                }
            }
        firestore.collection("gurus").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val skills = (doc.get("skills") as? List<*>)?.joinToString(", ") ?: ""
                    binding.etSkills.setText(skills)
                    binding.etExperience.setText(doc.getString("experience") ?: "")
                    binding.etAvailability.setText(doc.getString("availability") ?: "")
                    binding.etLocation.setText(doc.getString("location") ?: "")
                }
            }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                Glide.with(this).load(it).circleCrop().into(binding.ivProfilePhoto)
            }
        }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) { binding.tilName.error = getString(R.string.error_name_required); return }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        if (selectedImageUri != null) {
            val ref = storage.reference.child("avatars/$uid.jpg")
            ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        updateFirestore(uid, name, uri.toString())
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, "Photo upload failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            updateFirestore(uid, name, null)
        }
    }

    private fun updateFirestore(uid: String, name: String, photoUrl: String?) {
        val userUpdate = mutableMapOf<String, Any>("name" to name,
            "phone" to (binding.etPhone.text.toString().trim()))
        if (photoUrl != null) userUpdate["photoUrl"] = photoUrl

        val guruUpdate = mutableMapOf<String, Any>(
            "skills" to binding.etSkills.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
            "experience" to binding.etExperience.text.toString().trim(),
            "availability" to binding.etAvailability.text.toString().trim(),
            "location" to binding.etLocation.text.toString().trim()
        )
        if (photoUrl != null) guruUpdate["photoUrl"] = photoUrl

        firestore.collection("users").document(uid).update(userUpdate)
            .addOnSuccessListener {
                firestore.collection("gurus").document(uid).update(guruUpdate)
                    .addOnSuccessListener {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true
                        Toast.makeText(this, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        // Guru doc may not exist yet — set it
                        firestore.collection("gurus").document(uid).set(guruUpdate)
                            .addOnCompleteListener {
                                binding.progressBar.visibility = View.GONE
                                binding.btnSave.isEnabled = true
                                Toast.makeText(this, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
                                finish()
                            }
                    }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
