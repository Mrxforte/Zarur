package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Review
import com.example.zarur.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductReviewsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(productId: String): Flow<List<Review>> {
        return productRepository.getReviewsForProduct(productId)
    }
}
