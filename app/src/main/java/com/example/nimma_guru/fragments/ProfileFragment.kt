package com.example.nimma_guru.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nimma_guru.R
import com.example.nimma_guru.activities.EditProfileActivity
import com.example.nimma_guru.activities.LoginActivity
import com.example.nimma_guru.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var isEnglish = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()
        setupListeners()
    }

    private fun loadProfile() {
        val user = auth.currentUser ?: return
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                binding.tvProfileName.text = doc.getString("name") ?: user.email ?: "User"
                val role = doc.getString("role") ?: "student"
                binding.tvProfileRole.text = if (role == "guru")
                    getString(R.string.role_guru) else getString(R.string.role_student)
                val lang = doc.getString("language") ?: "en"
                isEnglish = lang == "en"
                binding.tvCurrentLanguage.text =
                    if (isEnglish) getString(R.string.language_english) else getString(R.string.language_kannada)
            }
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        binding.layoutLanguage.setOnClickListener { showLanguageDialog() }
        binding.layoutHelp.setOnClickListener {
            Toast.makeText(requireContext(), "Support: nimmaguruapp@gmail.com", Toast.LENGTH_LONG).show()
        }
        binding.btnLogout.setOnClickListener { confirmLogout() }
    }

    private fun showLanguageDialog() {
        val options = arrayOf(getString(R.string.language_english), getString(R.string.language_kannada))
        val current = if (isEnglish) 0 else 1
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.language_setting))
            .setSingleChoiceItems(options, current) { dialog, which ->
                isEnglish = which == 0
                val langCode = if (isEnglish) "en" else "kn"
                binding.tvCurrentLanguage.text = options[which]
                auth.currentUser?.uid?.let { uid ->
                    firestore.collection("users").document(uid).update("language", langCode)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                auth.signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
