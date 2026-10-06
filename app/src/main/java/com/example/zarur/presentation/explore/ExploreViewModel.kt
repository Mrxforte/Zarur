package com.example.zarur.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.usecase.GetPropertiesUseCase
import com.example.zarur.domain.usecase.GetUserProfileUseCase
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.domain.usecase.SearchPropertiesUseCase
import com.example.zarur.domain.usecase.SyncPropertiesUseCase
import com.example.zarur.domain.usecase.ToggleCartUseCase
import com.example.zarur.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getPropertiesUseCase: GetPropertiesUseCase,
    private val syncPropertiesUseCase: SyncPropertiesUseCase,
    private val searchPropertiesUseCase: SearchPropertiesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val toggleCartUseCase: ToggleCartUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _category = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val userProfile: StateFlow<User?> = getUserProfileUseCase()
        .map { (it as? Resource.Success)?.data }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val properties: StateFlow<List<Product>> = combine(_category, _searchQuery) { cat, query ->
        if (query.isNotEmpty()) {
            searchPropertiesUseCase(query)
        } else {
            getPropertiesUseCase(cat)
        }
    }.flatMapLatest { it }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        sync()
    }

    fun isLoggedIn(): Boolean = isLoggedInUseCase()

    fun sync() {
        viewModelScope.launch {
            syncPropertiesUseCase()
        }
    }

    fun setCategory(category: String?) {
        _category.value = category
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            toggleFavoriteUseCase(product.id, !product.isFavorite)
        }
    }

    fun addToCart(productId: String) {
        viewModelScope.launch {
            toggleCartUseCase(productId, true)
        }
    }
}
