package com.example.zarur.presentation.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.usecase.GetPropertiesUseCase
import com.example.zarur.domain.usecase.GetSellerProductsUseCase
import com.example.zarur.domain.usecase.ToggleCartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SellerStoreViewModel @Inject constructor(
    private val getSellerProductsUseCase: GetSellerProductsUseCase,
    private val getPropertiesUseCase: GetPropertiesUseCase,
    private val toggleCartUseCase: ToggleCartUseCase
) : ViewModel() {

    private val _sellerId = MutableStateFlow<String?>(null)

    val sellerProducts: StateFlow<List<Product>> = _sellerId
        .filterNotNull()
        .flatMapLatest { id -> getSellerProductsUseCase(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val relatedProducts: StateFlow<List<Product>> = _sellerId
        .filterNotNull()
        .flatMapLatest { id ->
            getPropertiesUseCase().map { allProducts ->
                allProducts.filter { it.sellerId != id }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadSeller(id: String) {
        _sellerId.value = id
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            toggleCartUseCase(product.id, true)
        }
    }
}
