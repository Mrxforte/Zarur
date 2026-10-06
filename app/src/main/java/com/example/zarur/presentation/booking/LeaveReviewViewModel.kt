package com.example.zarur.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.usecase.AddReviewUseCase
import com.example.zarur.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaveReviewViewModel @Inject constructor(
    private val addReviewUseCase: AddReviewUseCase
) : ViewModel() {

    private val _submitState = MutableStateFlow<UiState<Unit>?>(null)
    val submitState: StateFlow<UiState<Unit>?> = _submitState.asStateFlow()

    fun submitReview(productId: String, rating: Float, reviewText: String) {
        viewModelScope.launch {
            addReviewUseCase(productId, rating, reviewText).collect { resource ->
                _submitState.value = when (resource) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(resource.data)
                    is Resource.Error -> UiState.Error(resource.message)
                }
            }
        }
    }

    fun resetState() {
        _submitState.value = null
    }
}
