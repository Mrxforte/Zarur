package com.example.zarur.domain.usecase

import com.example.zarur.domain.repository.ProductRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(id: String, isFavorite: Boolean) {
        repository.toggleFavorite(id, isFavorite)
    }
}
