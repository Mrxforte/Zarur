package com.example.zarur.presentation.booking

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.R
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BookingInfoUiEvent {
    object NavigateToPayment : BookingInfoUiEvent
    data class ShowError(val message: String) : BookingInfoUiEvent
}

@HiltViewModel
class BookingInfoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<BookingInfoUiEvent>()
    val uiEvent: SharedFlow<BookingInfoUiEvent> = _uiEvent.asSharedFlow()

    val userProfile: StateFlow<User?> = getUserProfileUseCase()
        .map { (it as? Resource.Success)?.data }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun validateAndSubmit(
        fullName: String,
        nickname: String,
        dob: String,
        email: String,
        phone: String
    ) {
        if (fullName.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(BookingInfoUiEvent.ShowError(context.getString(R.string.error_enter_full_name))) }
            return
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewModelScope.launch { _uiEvent.emit(BookingInfoUiEvent.ShowError(context.getString(R.string.error_enter_valid_email))) }
            return
        }
        if (phone.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(BookingInfoUiEvent.ShowError(context.getString(R.string.error_enter_phone_number))) }
            return
        }

        viewModelScope.launch {
            _uiEvent.emit(BookingInfoUiEvent.NavigateToPayment)
        }
    }
}
