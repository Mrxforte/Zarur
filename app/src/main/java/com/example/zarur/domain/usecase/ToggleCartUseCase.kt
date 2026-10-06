package com.example.zarur.domain.usecase

import com.example.zarur.domain.repository.ProductRepository
import javax.inject.Inject

class ToggleCartUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(id: String, isInCart: Boolean) {
        productRepository.toggleCart(id, isInCart)
    }
}
