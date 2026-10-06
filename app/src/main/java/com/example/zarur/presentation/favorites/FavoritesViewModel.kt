package com.example.zarur.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.usecase.GetCartProductsUseCase
import com.example.zarur.domain.usecase.GetFavoritesUseCase
import com.example.zarur.domain.usecase.ToggleCartUseCase
import com.example.zarur.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavoritesUseCase: GetFavoritesUseCase,
    getCartProductsUseCase: GetCartProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val toggleCartUseCase: ToggleCartUseCase
) : ViewModel() {

    val favoriteProducts: StateFlow<List<Product>> = getFavoritesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartProducts: StateFlow<List<Product>> = getCartProductsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            toggleFavoriteUseCase(product.id, !product.isFavorite)
        }
    }

    fun removeFromCart(product: Product) {
        viewModelScope.launch {
            toggleCartUseCase(product.id, false)
        }
    }
}
