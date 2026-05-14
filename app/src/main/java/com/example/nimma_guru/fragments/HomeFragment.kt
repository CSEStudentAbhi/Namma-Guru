package com.example.nimma_guru.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nimma_guru.adapters.AppreciationAdapter
import com.example.nimma_guru.adapters.GuruAdapter
import com.example.nimma_guru.databinding.FragmentHomeBinding
import com.example.nimma_guru.model.Appreciation
import com.example.nimma_guru.model.Guru
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var guruAdapter: GuruAdapter
    private lateinit var fameAdapter: AppreciationAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private var gurusListener: ListenerRegistration? = null
    private var allGurus: List<Guru> = emptyList()
    private var selectedSkill: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserGreeting()
        setupAdapters()
        setupChipListeners()
        setupSearch()
        loadGurus()
        loadWallOfFame()

        binding.tvViewAll.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(com.example.nimma_guru.R.id.bottomNavigationView)?.selectedItemId = com.example.nimma_guru.R.id.searchFragment
        }
    }

    private fun setupUserGreeting() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: ""
                    binding.tvUserName.text = if (name.isNotEmpty()) "Hello, $name 👋" else ""
                }
        }
    }

    private fun setupAdapters() {
        guruAdapter = GuruAdapter { guru ->
            val intent = android.content.Intent(requireContext(), com.example.nimma_guru.activities.GuruProfileActivity::class.java).apply {
                putExtra("guruId", guru.guruId)
                putExtra("name", guru.name)
                putExtra("skills", guru.skills.joinToString(", "))
                putExtra("experience", guru.experience)
                putExtra("availability", guru.availability)
                putExtra("location", guru.location)
                putExtra("photoUrl", guru.photoUrl)
                putExtra("rating", guru.rating)
                putExtra("isAvailable", guru.isAvailable)
            }
            startActivity(intent)
        }
        binding.rvNearbyGurus.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNearbyGurus.adapter = guruAdapter

        fameAdapter = AppreciationAdapter()
        binding.rvWallOfFame.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvWallOfFame.adapter = fameAdapter
    }

    private fun setupChipListeners() {
        binding.chipGroupHome.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = if (checkedIds.isNotEmpty())
                group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0]) else null
            selectedSkill = chip?.text?.toString()?.takeIf { it != getString(com.example.nimma_guru.R.string.chip_all_skills) } ?: ""
            filterGurus()
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterGurus(query = s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadGurus() {
        gurusListener = firestore.collection("gurus")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                allGurus = snapshot.documents.mapNotNull { doc ->
                    val skills = doc.get("skills") as? List<*>
                    Guru(
                        guruId = doc.id,
                        name = doc.getString("name") ?: "",
                        skills = skills?.filterIsInstance<String>() ?: emptyList(),
                        experience = doc.getString("experience") ?: "",
                        availability = doc.getString("availability") ?: "",
                        location = doc.getString("location") ?: "",
                        photoUrl = doc.getString("photoUrl") ?: "",
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                        isAvailable = doc.getBoolean("isAvailable") ?: true
                    )
                }
                // If no Firestore data, use sample data
                if (allGurus.isEmpty()) allGurus = sampleGurus()
                filterGurus()
            }
    }

    private fun filterGurus(query: String = binding.etSearch.text.toString()) {
        var filtered = allGurus
        if (selectedSkill.isNotEmpty()) {
            filtered = filtered.filter { guru ->
                guru.skills.any { it.contains(selectedSkill, ignoreCase = true) }
            }
        }
        if (query.isNotEmpty()) {
            filtered = filtered.filter { guru ->
                guru.name.contains(query, ignoreCase = true) ||
                        guru.skills.any { it.contains(query, ignoreCase = true) } ||
                        guru.location.contains(query, ignoreCase = true)
            }
        }
        guruAdapter.submitList(filtered)
    }

    private fun loadWallOfFame() {
        firestore.collection("appreciation")
            .orderBy("count", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.mapNotNull { doc ->
                    Appreciation(
                        postId = doc.id,
                        guruId = doc.getString("guruId") ?: "",
                        guruName = doc.getString("guruName") ?: "",
                        guruSubject = doc.getString("guruSubject") ?: "",
                        count = (doc.getLong("count") ?: 0L).toInt()
                    )
                }
                if (items.isNotEmpty()) {
                    fameAdapter.submitList(items)
                    binding.rvWallOfFame.visibility = View.VISIBLE
                }
            }
    }

    private fun sampleGurus() = listOf(
        Guru("1", "Ramesh Kumar", listOf("Math"), "35", "Mon-Fri 4-6PM", "Jayanagar", "", 4.9f, 1.2f, true),
        Guru("2", "Lakshmi Devi", listOf("Science"), "28", "Weekends", "Basavanagudi", "", 4.8f, 2.5f, true),
        Guru("3", "Prakash Rao", listOf("Carpentry"), "42", "Sat-Sun", "Rajajinagar", "", 5.0f, 3.1f, false),
        Guru("4", "Anitha M", listOf("English"), "22", "Daily 5-7PM", "Malleshwaram", "", 4.7f, 0.8f, true),
        Guru("5", "Suresh B", listOf("Computer"), "15", "Evenings", "Koramangala", "", 4.6f, 4.0f, true)
    )

    override fun onDestroyView() {
        super.onDestroyView()
        gurusListener?.remove()
        _binding = null
    }
}
