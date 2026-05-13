package com.example.nimma_guru.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nimma_guru.R
import com.example.nimma_guru.databinding.ItemSessionCardBinding
import com.example.nimma_guru.model.Session
import com.google.firebase.firestore.FirebaseFirestore

class SessionAdapter : ListAdapter<Session, SessionAdapter.SessionViewHolder>(DiffCallback()) {

    inner class SessionViewHolder(private val binding: ItemSessionCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: Session) {
            binding.tvSessionTitle.text = session.title
            binding.tvGuruName.text = binding.root.context
                .getString(R.string.by_guru, session.mentorName)
            binding.tvDate.text = session.formattedDate()
            binding.tvTime.text = session.time
            binding.tvVenue.text = session.venue
            binding.tvAttendees.text = binding.root.context
                .getString(R.string.attendees, session.attendees, session.maxAttendees)

            binding.btnJoinSession.isEnabled = !session.isFull()

            binding.btnJoinSession.setOnClickListener {
                if (!session.isFull()) {
                    FirebaseFirestore.getInstance()
                        .collection("sessions")
                        .document(session.sessionId)
                        .update("attendees", session.attendees + 1)
                        .addOnSuccessListener {
                            android.widget.Toast.makeText(binding.root.context, "Joined session successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            android.widget.Toast.makeText(binding.root.context, "Failed to join session", android.widget.Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Session>() {
        override fun areItemsTheSame(old: Session, new: Session) = old.sessionId == new.sessionId
        override fun areContentsTheSame(old: Session, new: Session) = old == new
    }
}
