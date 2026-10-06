package com.example.zarur.domain.usecase

import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.repository.AuthRepository
import com.example.zarur.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class UploadProductUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        title: String,
        category: String,
        price: Double,
        description: String,
        imageUriString: String?
    ): Flow<Resource<Product>> = flow {
        val uid = authRepository.getCurrentUserId() ?: ""
        val product = Product(
            id = UUID.randomUUID().toString(),
            name = title,
            price = price,
            category = category,
            description = description,
            sellerId = uid,
            location = "Tashkent",
            imageUrl = imageUriString ?: ""
        )
        productRepository.uploadProduct(product, imageUriString).collect { res ->
            emit(res)
        }
    }
}
