package com.example.zarur.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.usecase.GetThemeUseCase
import com.example.zarur.domain.usecase.GetUserProfileUseCase
import com.example.zarur.domain.usecase.SetThemeUseCase
import com.example.zarur.domain.usecase.SignOutUseCase
import com.example.zarur.domain.usecase.UpdateUserProfileUseCase
import com.example.zarur.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = getThemeUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _profileState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val profileState: StateFlow<UiState<User>> = _profileState.asStateFlow()

    private val _updateEvent = MutableSharedFlow<UiState<Unit>>()
    val updateEvent: SharedFlow<UiState<Unit>> = _updateEvent.asSharedFlow()

    private var selectedAvatarUri: String? = null

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            getUserProfileUseCase().collect { resource ->
                _profileState.value = when (resource) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> {
                        val user = resource.data ?: User(fullName = "Andrew Ainsley", email = "andrew_ainsley@yourdomain.com")
                        UiState.Success(user)
                    }
                    is Resource.Error -> UiState.Error(resource.message)
                }
            }
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            setThemeUseCase(isDark)
        }
    }

    fun setSelectedAvatar(uriString: String) {
        selectedAvatarUri = uriString
    }

    fun updateProfile(
        fullName: String,
        nickname: String,
        email: String,
        phone: String,
        gender: String = "",
        dob: String = ""
    ) {
        viewModelScope.launch {
            updateUserProfileUseCase(
                fullName = fullName,
                nickname = nickname,
                email = email,
                phone = phone,
                gender = gender,
                dob = dob,
                imageUri = selectedAvatarUri
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _updateEvent.emit(UiState.Loading)
                    }
                    is Resource.Success -> {
                        _updateEvent.emit(UiState.Success(Unit))
                        loadUserProfile()
                    }
                    is Resource.Error -> {
                        _updateEvent.emit(UiState.Error(resource.message))
                    }
                }
            }
        }
    }

    fun signOut() {
        signOutUseCase()
    }
}
