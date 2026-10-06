package com.example.zarur.presentation.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zarur.databinding.ItemDigitalWalletBinding
import com.example.zarur.domain.model.PaymentMethod

class WalletsAdapter(
    private val isSelectionMode: Boolean = false,
    private val onWalletClick: (PaymentMethod) -> Unit,
    private val onActionClick: ((PaymentMethod) -> Unit)? = null
) : ListAdapter<PaymentMethod, WalletsAdapter.WalletViewHolder>(WalletDiffCallback()) {

    private var selectedMethodId: String? = null

    fun submitListWithSelection(list: List<PaymentMethod>?, selectedId: String?) {
        selectedMethodId = selectedId
        super.submitList(list)
    }

    fun setSelectedId(id: String?) {
        if (selectedMethodId == id) return
        selectedMethodId = id
        if (itemCount > 0) {
            notifyItemRangeChanged(0, itemCount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val binding = ItemDigitalWalletBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WalletViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WalletViewHolder(private val binding: ItemDigitalWalletBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(wallet: PaymentMethod) {
            binding.ivWalletIcon.setImageResource(wallet.iconResId)
            binding.tvWalletTitle.text = wallet.title
            binding.tvWalletSubtitle.text = wallet.formattedDisplayNumber

            if (isSelectionMode) {
                binding.btnAction.visibility = View.GONE
                binding.rbSelectWallet.visibility = View.VISIBLE
                binding.rbSelectWallet.isChecked = wallet.id == selectedMethodId
            } else {
                binding.rbSelectWallet.visibility = View.GONE
                binding.btnAction.visibility = View.VISIBLE
                binding.btnAction.text = if (wallet.isConnected) "Connected" else "Connect"
                binding.btnAction.setOnClickListener {
                    onActionClick?.invoke(wallet)
                }
            }

            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    setSelectedId(wallet.id)
                }
                onWalletClick(wallet)
            }
        }
    }

    private class WalletDiffCallback : DiffUtil.ItemCallback<PaymentMethod>() {
        override fun areItemsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean {
            return oldItem == newItem && oldItem.isConnected == newItem.isConnected
        }
    }
}
