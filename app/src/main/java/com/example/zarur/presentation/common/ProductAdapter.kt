package com.example.zarur.presentation.common

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.zarur.databinding.ItemProductCardBinding
import com.example.zarur.domain.model.Product

fun setupDotsIndicator(
    container: LinearLayout,
    count: Int,
    selectedIndex: Int
) {
    container.removeAllViews()
    if (count <= 1) {
        container.visibility = View.GONE
        return
    }
    container.visibility = View.VISIBLE
    val context = container.context
    val density = context.resources.displayMetrics.density

    for (i in 0 until count) {
        val dot = View(context)
        val isSelected = i == selectedIndex
        val widthDp = if (isSelected) 16 else 6
        val heightDp = 6

        val params = LinearLayout.LayoutParams(
            (widthDp * density).toInt(),
            (heightDp * density).toInt()
        ).apply {
            setMargins((3 * density).toInt(), 0, (3 * density).toInt(), 0)
        }
        dot.layoutParams = params

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 3f * density
            setColor(
                if (isSelected) 0xFFFFFFFF.toInt()
                else 0x90FFFFFF.toInt()
            )
        }
        dot.background = drawable
        container.addView(dot)
    }
}

class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onAddToCartClick: ((Product) -> Unit)? = null
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemProductCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvPrice.text = "%,.0f uzs".format(product.price).replace(",", " ")

            if (product.discount > 0) {
                binding.llDiscountRow.visibility = View.VISIBLE
                binding.tvDiscount.text = "-${product.discount}%"

                val originalPrice = (product.price * 100.0) / (100 - product.discount)
                binding.tvOriginalPrice.text = "%,.0f uzs".format(originalPrice).replace(",", " ")
                binding.tvOriginalPrice.paintFlags = binding.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.llDiscountRow.visibility = View.GONE
            }

            // Dynamic Rating & Minimal Review Count Display
            val countStr = if (product.reviewCount > 0) " (${product.reviewCount})" else ""
            binding.tvRating.text = "%.1f%s".format(product.rating, countStr)

            // Sold Status UI
            if (product.isSold) {
                binding.btnAddToCart.isEnabled = false
                binding.btnAddToCart.alpha = 0.5f
            } else {
                binding.btnAddToCart.isEnabled = true
                binding.btnAddToCart.alpha = 1.0f
            }

            // Image Gallery ViewPager2 with Transparent Bottom Dots Indicator
            val images = product.getAllImages()
            val sliderAdapter = ImageSliderAdapter(images) {
                onItemClick(product)
            }
            binding.vpProductGallery.adapter = sliderAdapter

            if (images.size > 1) {
                binding.llDotsIndicator.visibility = View.VISIBLE
                setupDotsIndicator(binding.llDotsIndicator, images.size, 0)

                binding.vpProductGallery.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        setupDotsIndicator(binding.llDotsIndicator, images.size, position)
                    }
                })
            } else {
                binding.llDotsIndicator.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(product) }
            binding.btnAddToCart.setOnClickListener { onAddToCartClick?.invoke(product) }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
            oldItem == newItem
    }
}
