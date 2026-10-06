package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPropertiesUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(query: String): Flow<List<Product>> = repository.searchProducts(query)
}
