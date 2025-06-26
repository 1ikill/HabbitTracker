package com.example.habbittracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habbittracker.domain.model.User
import com.example.habbittracker.domain.usecase.auth.GetCurrentUserUseCase
import com.example.habbittracker.domain.usecase.auth.SignInUseCase
import com.example.habbittracker.domain.usecase.auth.SignOutUseCase
import com.example.habbittracker.domain.usecase.auth.SignUpUseCase
import com.example.habbittracker.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isSignedIn: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _authState.value = _authState.value.copy(
                            user = resource.data,
                            isSignedIn = resource.data != null,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _authState.value = _authState.value.copy(
                            user = null,
                            isSignedIn = false,
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    is Resource.Loading -> {
                        _authState.value = _authState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                }
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            signUpUseCase(email, password, displayName).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _authState.value = _authState.value.copy(
                            user = resource.data,
                            isSignedIn = true,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    is Resource.Loading -> {
                        _authState.value = _authState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            signInUseCase(email, password).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _authState.value = _authState.value.copy(
                            user = resource.data,
                            isSignedIn = true,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    is Resource.Loading -> {
                        _authState.value = _authState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _authState.value = _authState.value.copy(
                            user = null,
                            isSignedIn = false,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    is Resource.Loading -> {
                        _authState.value = _authState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
