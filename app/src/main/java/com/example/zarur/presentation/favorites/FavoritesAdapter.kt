package com.example.zarur.presentation.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.zarur.databinding.ItemFavoritePropertyCardBinding
import com.example.zarur.databinding.ItemFavoritePropertyListBinding

class FavoritesAdapter(
    private val onItemClick: (Int) -> Unit,
    private val onFavoriteClick: (Int) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    var isGridView: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val items = (1..10).toList() // Mock data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (isGridView) {
            FavoriteViewHolder.Card(
                ItemFavoritePropertyCardBinding.inflate(inflater, parent, false)
            )
        } else {
            FavoriteViewHolder.List(
                ItemFavoritePropertyListBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        when (holder) {
            is FavoriteViewHolder.Card -> {
                holder.binding.root.setOnClickListener { onItemClick(position) }
                holder.binding.ivFavorite.setOnClickListener { onFavoriteClick(position) }
            }
            is FavoriteViewHolder.List -> {
                holder.binding.root.setOnClickListener { onItemClick(position) }
                holder.binding.ivFavorite.setOnClickListener { onFavoriteClick(position) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    sealed class FavoriteViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        class Card(val binding: ItemFavoritePropertyCardBinding) : FavoriteViewHolder(binding)
        class List(val binding: ItemFavoritePropertyListBinding) : FavoriteViewHolder(binding)
    }
}
