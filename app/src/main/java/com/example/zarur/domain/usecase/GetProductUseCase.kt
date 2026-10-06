package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(id: String): Flow<Product?> = repository.getProductById(id)
}
