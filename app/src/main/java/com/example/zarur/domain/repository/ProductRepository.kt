package com.example.zarur.domain.repository

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(category: String? = null): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    fun getCartProducts(): Flow<List<Product>>
    fun getSellerProducts(sellerId: String): Flow<List<Product>>
    fun uploadProduct(product: Product, imageUriString: String?): Flow<Resource<Product>>
    fun getReviewsForProduct(productId: String): Flow<List<Review>>
    fun addReview(review: Review): Flow<Resource<Unit>>
    suspend fun syncProducts()
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    suspend fun toggleCart(id: String, isInCart: Boolean)
}
