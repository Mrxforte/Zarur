package com.example.zarur.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.PaymentMethod
import com.example.zarur.domain.model.PaymentType
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val cards: List<PaymentMethod> = emptyList(),
    val wallets: List<PaymentMethod> = emptyList(),
    val selectedMethodId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val addCardSuccessMessage: String? = null,
    val paymentSuccessTxnId: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        observePaymentMethods()
    }

    private fun observePaymentMethods() {
        viewModelScope.launch {
            paymentRepository.getPaymentMethods().collect { methods ->
                val cards = methods.filter { it.type == PaymentType.CARD }
                val wallets = methods.filter { it.type == PaymentType.DIGITAL_WALLET }
                
                val currentSelected = _uiState.value.selectedMethodId
                val defaultCard = cards.find { it.isDefault }?.id ?: cards.firstOrNull()?.id

                _uiState.update { state ->
                    state.copy(
                        cards = cards,
                        wallets = wallets,
                        selectedMethodId = currentSelected ?: defaultCard
                    )
                }
            }
        }
    }

    fun selectMethod(methodId: String) {
        _uiState.update { it.copy(selectedMethodId = methodId) }
    }

    fun addCard(
        cardHolderName: String,
        cardNumber: String,
        expiryDate: String,
        cvv: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, addCardSuccessMessage = null) }
            val result = paymentRepository.addPaymentCard(
                cardHolderName = cardHolderName,
                cardNumber = cardNumber,
                expiryDate = expiryDate,
                cvv = cvv,
                isDefault = isDefault
            )
            when (result) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            addCardSuccessMessage = "Card successfully added and verified!",
                            selectedMethodId = result.data.id
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun deleteCard(id: String) {
        viewModelScope.launch {
            paymentRepository.deletePaymentMethod(id)
        }
    }

    fun setDefaultCard(id: String) {
        viewModelScope.launch {
            paymentRepository.setDefaultPaymentMethod(id)
        }
    }

    fun toggleWallet(id: String) {
        viewModelScope.launch {
            paymentRepository.toggleWalletConnection(id)
        }
    }

    fun processPayment(amount: Double) {
        val selectedId = _uiState.value.selectedMethodId ?: return
        viewModelScope.launch {
            paymentRepository.processPaymentSimulation(selectedId, amount).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, paymentSuccessTxnId = resource.data)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                error = null,
                addCardSuccessMessage = null,
                paymentSuccessTxnId = null
            )
        }
    }
}
