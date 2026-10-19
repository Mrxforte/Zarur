package com.example.zarur.domain.usecase

import com.example.zarur.data.local.AppPreferences
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.Review
import com.example.zarur.domain.repository.AuthRepository
import com.example.zarur.domain.repository.ProductRepository
import com.example.zarur.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class AddReviewUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences
) {
    operator fun invoke(
        productId: String,
        rating: Float,
        reviewText: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val uid = authRepository.getCurrentUserId() ?: ""
        var userName = appPreferences.userName.ifBlank { "Verified Buyer" }
        var userAvatar: String? = appPreferences.userAvatarUrl.ifBlank { null }

        if (uid.isNotEmpty()) {
            try {
                userRepository.getUserProfile(uid).collect { userRes ->
                    if (userRes is Resource.Success && userRes.data != null) {
                        val user = userRes.data
                        if (user.fullName.isNotBlank()) userName = user.fullName
                        if (!user.avatarUrl.isNullOrBlank()) userAvatar = user.avatarUrl
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val review = Review(
            id = UUID.randomUUID().toString(),
            productId = productId,
            userId = uid,
            userName = userName,
            userAvatarUrl = userAvatar,
            rating = rating,
            reviewText = reviewText,
            timestamp = System.currentTimeMillis()
        )

        productRepository.addReview(review).collect { res ->
            emit(res)
        }
    }
}
