package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.repository.AuthRepository
import com.example.zarur.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetSellerProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(sellerId: String? = null): Flow<List<Product>> {
        val targetUid = if (!sellerId.isNullOrEmpty()) sellerId else (authRepository.getCurrentUserId() ?: return flowOf(emptyList()))
        return productRepository.getSellerProducts(targetUid)
    }
}
