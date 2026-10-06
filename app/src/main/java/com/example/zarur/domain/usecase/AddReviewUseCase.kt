package com.example.zarur.domain.usecase

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
    private val userRepository: UserRepository
) {
    operator fun invoke(
        productId: String,
        rating: Float,
        reviewText: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val uid = authRepository.getCurrentUserId() ?: ""
        var userName = "Verified Buyer"
        var userAvatar: String? = null

        if (uid.isNotEmpty()) {
            userRepository.getUserProfile(uid).collect { userRes ->
                if (userRes is Resource.Success && userRes.data != null) {
                    val user = userRes.data
                    if (user.fullName.isNotEmpty()) userName = user.fullName
                    userAvatar = user.avatarUrl
                }
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
