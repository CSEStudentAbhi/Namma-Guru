package com.example.nimma_guru.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nimma_guru.R
import com.example.nimma_guru.databinding.ItemWallOfFameBinding
import com.example.nimma_guru.model.Appreciation

class AppreciationAdapter : ListAdapter<Appreciation, AppreciationAdapter.FameViewHolder>(DiffCallback()) {

    inner class FameViewHolder(private val binding: ItemWallOfFameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Appreciation) {
            binding.tvFameName.text = item.guruName
            binding.tvFameSubject.text = item.guruSubject
            binding.tvFameThanks.text = binding.root.context
                .getString(R.string.appreciation_posted).let {
                    "${item.count} thanks"
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FameViewHolder {
        val binding = ItemWallOfFameBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Appreciation>() {
        override fun areItemsTheSame(old: Appreciation, new: Appreciation) = old.guruId == new.guruId
        override fun areContentsTheSame(old: Appreciation, new: Appreciation) = old == new
    }
}
