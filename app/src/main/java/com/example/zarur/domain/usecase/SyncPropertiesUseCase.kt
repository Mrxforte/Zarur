package com.example.zarur.domain.usecase

import com.example.zarur.domain.repository.ProductRepository
import javax.inject.Inject

class SyncPropertiesUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke() = repository.syncProducts()
}
