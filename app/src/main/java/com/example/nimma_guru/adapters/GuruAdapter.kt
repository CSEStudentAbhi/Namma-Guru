package com.example.nimma_guru.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nimma_guru.R
import com.example.nimma_guru.databinding.ItemGuruCardBinding
import com.example.nimma_guru.model.Guru

class GuruAdapter(
    private val onItemClick: (Guru) -> Unit = {}
) : ListAdapter<Guru, GuruAdapter.GuruViewHolder>(DiffCallback()) {

    inner class GuruViewHolder(private val binding: ItemGuruCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(guru: Guru) {
            binding.tvName.text = guru.name
            binding.tvSubject.text = guru.primarySkill
            binding.tvExperience.text = binding.root.context
                .getString(R.string.years_experience, guru.experience.toIntOrNull() ?: 0)
            binding.tvRating.text = String.format("%.1f", guru.rating)
            binding.tvDistance.text = binding.root.context
                .getString(R.string.km_away, guru.distanceKm)

            if (guru.isAvailable) {
                binding.tvStatus.setText(R.string.available)
                binding.tvStatus.setTextColor(
                    binding.root.context.getColor(R.color.status_available)
                )
                binding.statusDot.setBackgroundResource(R.drawable.bg_status_dot_green)
            } else {
                binding.tvStatus.setText(R.string.busy)
                binding.tvStatus.setTextColor(
                    binding.root.context.getColor(R.color.status_busy)
                )
                binding.statusDot.setBackgroundResource(R.drawable.bg_status_dot_grey)
            }

            if (guru.photoUrl.isNotEmpty()) {
                Glide.with(binding.ivAvatar)
                    .load(guru.photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(binding.ivAvatar)
            }

            binding.root.setOnClickListener { onItemClick(guru) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
        val binding = ItemGuruCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GuruViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Guru>() {
        override fun areItemsTheSame(oldItem: Guru, newItem: Guru) =
            oldItem.guruId == newItem.guruId
        override fun areContentsTheSame(oldItem: Guru, newItem: Guru) = oldItem == newItem
    }
}
