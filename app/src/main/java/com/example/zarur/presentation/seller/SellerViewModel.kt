package com.example.zarur.presentation.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.Product
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.usecase.GetSellerProductsUseCase
import com.example.zarur.domain.usecase.IsLoggedInUseCase
import com.example.zarur.domain.usecase.UploadProductUseCase
import com.example.zarur.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerViewModel @Inject constructor(
    private val uploadProductUseCase: UploadProductUseCase,
    private val getSellerProductsUseCase: GetSellerProductsUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UiState<Product>?>(null)
    val uploadState: StateFlow<UiState<Product>?> = _uploadState.asStateFlow()

    private var selectedPhotoUri: String? = null

    val sellerProducts: StateFlow<List<Product>> = getSellerProductsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedPhoto(uriString: String) {
        selectedPhotoUri = uriString
    }

    fun uploadProduct(
        title: String,
        category: String,
        price: Double,
        description: String
    ) {
        if (!isLoggedInUseCase()) {
            _uploadState.value = UiState.Error("AUTH_REQUIRED")
            return
        }
        viewModelScope.launch {
            uploadProductUseCase(
                title = title,
                category = category,
                price = price,
                description = description,
                imageUriString = selectedPhotoUri
            ).collect { resource ->
                _uploadState.value = when (resource) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(resource.data)
                    is Resource.Error -> UiState.Error(resource.message)
                }
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = null
        selectedPhotoUri = null
    }
}
