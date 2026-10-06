package com.example.zarur.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zarur.domain.model.Resource
import com.example.zarur.domain.model.User
import com.example.zarur.domain.usecase.SignInUseCase
import com.example.zarur.domain.usecase.SignInWithGoogleUseCase
import com.example.zarur.domain.usecase.SignUpUseCase
import com.example.zarur.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _signInState = MutableStateFlow<UiState<User>?>(null)
    val signInState: StateFlow<UiState<User>?> = _signInState.asStateFlow()

    private val _signUpState = MutableStateFlow<UiState<User>?>(null)
    val signUpState: StateFlow<UiState<User>?> = _signUpState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            signInUseCase(email, password).collect { resource ->
                _signInState.value = when (resource) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(resource.data)
                    is Resource.Error -> UiState.Error(resource.message)
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            signInWithGoogleUseCase(idToken).collect { resource ->
                _signInState.value = when (resource) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(resource.data)
                    is Resource.Error -> UiState.Error(resource.message)
                }
            }
        }
    }

    fun signUp(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            signUpUseCase(email, password, fullName).collect { resource ->
                _signUpState.value = when (resource) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(resource.data)
                    is Resource.Error -> UiState.Error(resource.message)
                }
            }
        }
    }

    fun resetSignInState() {
        _signInState.value = null
    }

    fun resetSignUpState() {
        _signUpState.value = null
    }
}
