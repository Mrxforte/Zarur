package com.example.zarur.presentation.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.ItemReviewCardBinding
import com.example.zarur.domain.model.Review

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemReviewCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvUserName.text = review.userName
            binding.tvRating.text = "%.1f".format(review.rating)
            binding.tvReviewText.text = review.reviewText

            if (!review.userAvatarUrl.isNullOrEmpty()) {
                Glide.with(binding.ivAvatar)
                    .load(review.userAvatarUrl)
                    .placeholder(R.drawable.onboarding1)
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.onboarding1)
            }

            val diffDays = (System.currentTimeMillis() - review.timestamp) / (1000 * 60 * 60 * 24)
            binding.tvDate.text = when {
                diffDays <= 0 -> "Today"
                diffDays == 1L -> "Yesterday"
                else -> "$diffDays days ago"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }
}
