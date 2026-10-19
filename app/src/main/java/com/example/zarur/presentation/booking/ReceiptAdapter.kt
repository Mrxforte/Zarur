package com.example.zarur.presentation.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.ItemReceiptCardBinding
import com.example.zarur.domain.model.Receipt

class ReceiptAdapter(
    private val onItemClick: (Receipt) -> Unit
) : ListAdapter<Receipt, ReceiptAdapter.ReceiptViewHolder>(ReceiptDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val binding = ItemReceiptCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReceiptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReceiptViewHolder(
        private val binding: ItemReceiptCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(receipt: Receipt) {
            binding.tvTitle.text = receipt.productName
            binding.tvTransactionId.text = "ID: #${receipt.transactionId}"
            binding.tvDate.text = receipt.date
            binding.tvPrice.text = receipt.priceFormatted
            binding.tvStatus.text = receipt.status

            if (receipt.productImage.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(receipt.productImage)
                    .placeholder(R.drawable.ic_e_receipt)
                    .into(binding.ivProduct)
            } else {
                binding.ivProduct.setImageResource(R.drawable.ic_e_receipt)
            }

            itemView.setOnClickListener {
                onItemClick(receipt)
            }
        }
    }

    private class ReceiptDiffCallback : DiffUtil.ItemCallback<Receipt>() {
        override fun areItemsTheSame(oldItem: Receipt, newItem: Receipt): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Receipt, newItem: Receipt): Boolean {
            return oldItem == newItem
        }
    }
}
