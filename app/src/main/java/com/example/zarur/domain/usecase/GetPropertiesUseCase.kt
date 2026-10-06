package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPropertiesUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(category: String? = null): Flow<List<Product>> {
        return repository.getProducts(category)
    }
}
