package com.example.zarur.presentation.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.zarur.R
import com.example.zarur.databinding.ItemFavoritePropertyCardBinding
import com.example.zarur.databinding.ItemFavoritePropertyListBinding
import com.example.zarur.domain.model.Product

class FavoritesAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onFavoriteClick: (Product) -> Unit,
    private val onAddToCartClick: ((Product) -> Unit)? = null
) : ListAdapter<Product, FavoritesAdapter.FavoriteViewHolder>(ProductDiffCallback()) {

    var isGridView: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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
        val product = getItem(position)
        when (holder) {
            is FavoriteViewHolder.Card -> holder.bind(product, onItemClick, onFavoriteClick, onAddToCartClick)
            is FavoriteViewHolder.List -> holder.bind(product, onItemClick, onFavoriteClick)
        }
    }

    sealed class FavoriteViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        class Card(val binding: ItemFavoritePropertyCardBinding) : FavoriteViewHolder(binding) {
            fun bind(
                product: Product,
                onItemClick: (Product) -> Unit,
                onFavoriteClick: (Product) -> Unit,
                onAddToCartClick: ((Product) -> Unit)?
            ) {
                binding.tvPropertyName.text = product.name
                binding.tvPrice.text = "%,.0f uzs".format(product.price).replace(",", " ")
                
                Glide.with(binding.ivProperty)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.onboarding3)
                    .into(binding.ivProperty)

                binding.ivProperty.alpha = if (product.isSold) 0.5f else 1.0f
                    
                binding.root.setOnClickListener { onItemClick(product) }
                binding.ivFavorite.setOnClickListener { onFavoriteClick(product) }
                binding.btnAddToCart.setOnClickListener { onAddToCartClick?.invoke(product) }
            }
        }
        
        class List(val binding: ItemFavoritePropertyListBinding) : FavoriteViewHolder(binding) {
            fun bind(
                product: Product,
                onItemClick: (Product) -> Unit,
                onFavoriteClick: (Product) -> Unit
            ) {
                binding.tvPropertyName.text = product.name
                binding.tvPrice.text = "%,.0f uzs".format(product.price).replace(",", " ")
                
                Glide.with(binding.ivProperty)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.onboarding3)
                    .into(binding.ivProperty)
                    
                binding.root.setOnClickListener { onItemClick(product) }
                binding.ivFavorite.setOnClickListener { onFavoriteClick(product) }
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem == newItem
    }
}
