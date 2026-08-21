package com.example.zarur.presentation.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.databinding.ItemPropertyRecommendationBinding

class PropertyAdapter(
    private val onItemClick: () -> Unit
) : RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    private val items = (1..10).toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyRecommendationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PropertyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.binding.root.setOnClickListener { onItemClick() }
    }

    override fun getItemCount(): Int = items.size

    class PropertyViewHolder(val binding: ItemPropertyRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root)
}
