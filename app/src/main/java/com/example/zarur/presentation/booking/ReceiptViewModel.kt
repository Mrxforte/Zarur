package com.example.zarur.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.Receipt
import com.example.zarur.domain.usecase.GetReceiptsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    getReceiptsUseCase: GetReceiptsUseCase
) : ViewModel() {

    val receipts: StateFlow<List<Receipt>> = getReceiptsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
