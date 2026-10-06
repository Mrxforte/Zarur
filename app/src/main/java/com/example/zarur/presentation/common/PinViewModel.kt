package com.example.zarur.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.usecase.IsPinSetUseCase
import com.example.zarur.domain.usecase.SavePinUseCase
import com.example.zarur.domain.usecase.ValidatePinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val savePinUseCase: SavePinUseCase,
    private val validatePinUseCase: ValidatePinUseCase,
    private val isPinSetUseCase: IsPinSetUseCase
) : ViewModel() {

    private val _pinState = MutableStateFlow("")
    val pinState: StateFlow<String> = _pinState

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent

    private val _successEvent = MutableSharedFlow<Unit>()
    val successEvent: SharedFlow<Unit> = _successEvent

    fun onNumberClick(number: String) {
        if (_pinState.value.length < 4) {
            _pinState.value += number
        }
    }

    fun onDeleteClick() {
        if (_pinState.value.isNotEmpty()) {
            _pinState.value = _pinState.value.dropLast(1)
        }
    }

    fun savePin() {
        if (_pinState.value.length == 4) {
            viewModelScope.launch {
                savePinUseCase(_pinState.value)
                _successEvent.emit(Unit)
            }
        }
    }

    suspend fun checkIsPinSet(): Boolean = isPinSetUseCase()

    fun validatePin() {
        if (_pinState.value.length == 4) {
            viewModelScope.launch {
                savePinUseCase(_pinState.value)
                _successEvent.emit(Unit)
            }
        }
    }
}
