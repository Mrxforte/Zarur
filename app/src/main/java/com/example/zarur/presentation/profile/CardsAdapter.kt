package com.example.zarur.presentation.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.databinding.ItemCreditCardBinding
import com.example.zarur.domain.model.PaymentMethod

class CardsAdapter(
    private val isSelectionMode: Boolean = false,
    private val onCardClick: (PaymentMethod) -> Unit,
    private val onDeleteClick: ((PaymentMethod) -> Unit)? = null
) : ListAdapter<PaymentMethod, CardsAdapter.CardViewHolder>(CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCreditCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardViewHolder(private val binding: ItemCreditCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: PaymentMethod) {
            binding.clCardBackground.setBackgroundResource(card.bgResId)
            binding.ivBrandLogo.setImageResource(card.iconResId)
            binding.tvCardNumber.text = card.formattedDisplayNumber
            binding.tvCardHolder.text = card.cardHolderName.ifEmpty { "CARD HOLDER" }
            binding.tvExpiryDate.text = card.expiryDate.ifEmpty { "MM/YY" }

            binding.tvDefaultBadge.visibility = if (card.isDefault) View.VISIBLE else View.GONE

            if (isSelectionMode) {
                binding.btnDeleteCard.visibility = View.GONE
                binding.ivSelectionCheck.visibility = if (card.isSelected) View.VISIBLE else View.GONE
            } else {
                binding.ivSelectionCheck.visibility = View.GONE
                binding.btnDeleteCard.visibility = if (onDeleteClick != null) View.VISIBLE else View.GONE
                binding.btnDeleteCard.setOnClickListener {
                    onDeleteClick?.invoke(card)
                }
            }

            binding.root.setOnClickListener {
                onCardClick(card)
            }
        }
    }

    private class CardDiffCallback : DiffUtil.ItemCallback<PaymentMethod>() {
        override fun areItemsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean {
            return oldItem == newItem
        }
    }
}
