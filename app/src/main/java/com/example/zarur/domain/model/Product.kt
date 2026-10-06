package com.example.zarur.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String = "uzs",
    val discount: Int = 0,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val installment: String? = null,
    val imageUrl: String,
    val images: List<String> = emptyList(),
    val category: String,
    val location: String = "Tashkent",
    val description: String = "",
    val sellerId: String = "",
    val isFavorite: Boolean = false,
    val isInCart: Boolean = false,
    val isSold: Boolean = false,
    val attributes: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getAllImages(): List<String> {
        return if (images.isNotEmpty()) images else listOf(imageUrl)
    }
}
