package com.example.zarur.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zarur.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val currency: String,
    val discount: Int,
    val rating: Float,
    val reviewCount: Int = 0,
    val installment: String?,
    val imageUrl: String,
    val imagesCsv: String = "",
    val category: String,
    val location: String,
    val description: String = "",
    val sellerId: String = "",
    val isFavorite: Boolean,
    val isInCart: Boolean,
    val isSold: Boolean,
    val timestamp: Long
) {
    fun toDomain(): Product = Product(
        id = id,
        name = name,
        price = price,
        currency = currency,
        discount = discount,
        rating = rating,
        reviewCount = reviewCount,
        installment = installment,
        imageUrl = imageUrl,
        images = if (imagesCsv.isNotEmpty()) imagesCsv.split(",") else listOf(imageUrl),
        category = category,
        location = location,
        description = description,
        sellerId = sellerId,
        isFavorite = isFavorite,
        isInCart = isInCart,
        isSold = isSold,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            name = product.name,
            price = product.price,
            currency = product.currency,
            discount = product.discount,
            rating = product.rating,
            reviewCount = product.reviewCount,
            installment = product.installment,
            imageUrl = product.imageUrl,
            imagesCsv = product.getAllImages().joinToString(","),
            category = product.category,
            location = product.location,
            description = product.description,
            sellerId = product.sellerId,
            isFavorite = product.isFavorite,
            isInCart = product.isInCart,
            isSold = product.isSold,
            timestamp = product.timestamp
        )
    }
}
