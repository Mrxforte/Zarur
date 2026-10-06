package com.example.zarur.presentation.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.ItemImageSlideBinding

class ImageSliderAdapter(
    private val images: List<String>,
    private val onItemClick: (() -> Unit)? = null
) : RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(val binding: ItemImageSlideBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemImageSlideBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(images[position])
            .placeholder(R.drawable.onboarding1)
            .error(R.drawable.onboarding1)
            .into(holder.binding.ivSlideImage)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke()
        }
    }

    override fun getItemCount(): Int = images.size
}
