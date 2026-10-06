package com.example.zarur.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.Review
import com.example.zarur.domain.model.User
import com.example.zarur.domain.usecase.AddReviewUseCase
import com.example.zarur.domain.usecase.GetProductReviewsUseCase
import com.example.zarur.domain.usecase.GetProductUseCase
import com.example.zarur.domain.usecase.GetPropertiesUseCase
import com.example.zarur.domain.usecase.GetUserProfileUseCase
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.domain.usecase.ToggleCartUseCase
import com.example.zarur.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

sealed interface ProductDetailUiEvent {
    object NavigateToAuth : ProductDetailUiEvent
    object ShowAddToCartSuccess : ProductDetailUiEvent
    object NavigateToBooking : ProductDetailUiEvent
    object NavigateToChat : ProductDetailUiEvent
    object ShowReviewSuccess : ProductDetailUiEvent
    data class ShowReviewError(val message: String) : ProductDetailUiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductUseCase: GetProductUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val toggleCartUseCase: ToggleCartUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val getProductReviewsUseCase: GetProductReviewsUseCase,
    private val addReviewUseCase: AddReviewUseCase,
    private val getPropertiesUseCase: GetPropertiesUseCase,
    getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _productId = MutableStateFlow<String?>(null)
    private val _uiEvent = MutableSharedFlow<ProductDetailUiEvent>()
    val uiEvent: SharedFlow<ProductDetailUiEvent> = _uiEvent.asSharedFlow()
    
    val product: StateFlow<Product?> = _productId
        .filterNotNull()
        .flatMapLatest { getProductUseCase(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val reviews: StateFlow<List<Review>> = _productId
        .filterNotNull()
        .flatMapLatest { getProductReviewsUseCase(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<User?> = getUserProfileUseCase()
        .map { (it as? Resource.Success)?.data }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val relatedProducts: StateFlow<List<Product>> = _productId
        .filterNotNull()
        .flatMapLatest { id ->
            val cat = product.value?.category
            getPropertiesUseCase(cat).map { list ->
                list.filter { it.id != id }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadProduct(id: String) {
        _productId.value = id
    }

    fun submitReview(reviewText: String, rating: Float = 5.0f) {
        if (!isLoggedInUseCase()) {
            viewModelScope.launch { _uiEvent.emit(ProductDetailUiEvent.NavigateToAuth) }
            return
        }
        val currentProductId = _productId.value ?: return
        if (reviewText.isBlank()) return

        viewModelScope.launch {
            addReviewUseCase(currentProductId, rating, reviewText).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        _uiEvent.emit(ProductDetailUiEvent.ShowReviewSuccess)
                        _productId.value = currentProductId
                    }
                    is Resource.Error -> {
                        _uiEvent.emit(ProductDetailUiEvent.ShowReviewError(resource.message))
                    }
                }
            }
        }
    }

    fun toggleFavorite() {
        if (!isLoggedInUseCase()) {
            viewModelScope.launch { _uiEvent.emit(ProductDetailUiEvent.NavigateToAuth) }
            return
        }
        val currentProduct = product.value ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(currentProduct.id, !currentProduct.isFavorite)
        }
    }

    fun addToCart() {
        if (!isLoggedInUseCase()) {
            viewModelScope.launch { _uiEvent.emit(ProductDetailUiEvent.NavigateToAuth) }
            return
        }
        val currentProduct = product.value ?: return
        viewModelScope.launch {
            toggleCartUseCase(currentProduct.id, true)
            _uiEvent.emit(ProductDetailUiEvent.ShowAddToCartSuccess)
        }
    }

    fun buyNow() {
        if (!isLoggedInUseCase()) {
            viewModelScope.launch { _uiEvent.emit(ProductDetailUiEvent.NavigateToAuth) }
            return
        }
        viewModelScope.launch {
            _uiEvent.emit(ProductDetailUiEvent.NavigateToBooking)
        }
    }

    fun chat() {
        if (!isLoggedInUseCase()) {
            viewModelScope.launch { _uiEvent.emit(ProductDetailUiEvent.NavigateToAuth) }
            return
        }
        viewModelScope.launch {
            _uiEvent.emit(ProductDetailUiEvent.NavigateToChat)
        }
    }
}
