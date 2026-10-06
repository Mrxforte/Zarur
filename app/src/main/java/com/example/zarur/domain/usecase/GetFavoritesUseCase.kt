package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        // We map the general products list to only return ones that are favorites
        // In a real production app with thousands of items, we would write a direct SQL query
        // `SELECT * FROM products WHERE isFavorite = 1` inside ProductDao.
        // For this architecture demo, filtering the Flow works perfectly.
        return repository.getProducts().map { products ->
            products.filter { it.isFavorite }
        }
    }
}
