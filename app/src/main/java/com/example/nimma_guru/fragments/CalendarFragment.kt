package com.example.nimma_guru.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nimma_guru.adapters.SessionAdapter
import com.example.nimma_guru.databinding.FragmentCalendarBinding
import com.example.nimma_guru.model.Session
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionAdapter: SessionAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private var sessionsListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionAdapter = SessionAdapter()
        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSessions.adapter = sessionAdapter
        loadSessions()
    }

    private fun loadSessions() {
        sessionsListener = firestore.collection("sessions")
            .whereGreaterThan("date", Timestamp.now())
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    // Fall back to sample sessions
                    showSampleSessions()
                    return@addSnapshotListener
                }
                val sessions = snapshot.documents.mapNotNull { doc ->
                    Session(
                        sessionId = doc.id,
                        mentorId = doc.getString("mentorId") ?: "",
                        mentorName = doc.getString("mentorName") ?: "",
                        title = doc.getString("title") ?: "",
                        date = doc.getTimestamp("date"),
                        time = doc.getString("time") ?: "",
                        venue = doc.getString("venue") ?: "",
                        attendees = (doc.getLong("attendees") ?: 0L).toInt(),
                        maxAttendees = (doc.getLong("maxAttendees") ?: 20L).toInt()
                    )
                }
                val list = if (sessions.isEmpty()) sampleSessions() else sessions
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvSessions.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                sessionAdapter.submitList(list)
            }
    }

    private fun showSampleSessions() {
        val sample = sampleSessions()
        binding.tvEmpty.visibility = if (sample.isEmpty()) View.VISIBLE else View.GONE
        binding.rvSessions.visibility = if (sample.isEmpty()) View.GONE else View.VISIBLE
        sessionAdapter.submitList(sample)
    }

    private fun sampleSessions() = listOf(
        Session("s1", "1", "Ramesh Kumar", "Basic Mathematics – Algebra Fundamentals",
            null, "10:00 AM – 12:00 PM", "Community Center, Jayanagar", 15, 20),
        Session("s2", "2", "Lakshmi Devi", "Science – Human Body Systems",
            null, "4:00 PM – 5:30 PM", "Basavanagudi Library", 8, 15),
        Session("s3", "4", "Anitha M", "English Speaking Workshop",
            null, "6:00 PM – 7:30 PM", "Malleshwaram Community Hall", 12, 25),
        Session("s4", "5", "Suresh B", "Computer Basics for Beginners",
            null, "9:00 AM – 11:00 AM", "Koramangala Digital Center", 6, 10)
    )

    override fun onDestroyView() {
        super.onDestroyView()
        sessionsListener?.remove()
        _binding = null
    }
}
