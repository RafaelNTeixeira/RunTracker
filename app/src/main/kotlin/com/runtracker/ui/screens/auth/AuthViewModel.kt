package com.runtracker.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtracker.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: UserRepository
) : ViewModel() {

    sealed class State {
        object Idle    : State()
        object Loading : State()
        object Success : State()
        data class Error(val msg: String) : State()
    }

    /** null = still loading from DataStore, true = logged in, false = not */
    val isLoggedIn: StateFlow<Boolean?> = repo.loggedInUsername
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    fun login(username: String, password: String) = launch {
        val result = repo.login(username, password)
        _state.value = if (result.isSuccess) State.Success
        else State.Error(result.exceptionOrNull()?.message ?: "Login failed")
    }

    fun register(username: String, email: String, password: String) = launch {
        val result = repo.register(username, email, password)
        _state.value = if (result.isSuccess) State.Success
        else State.Error(result.exceptionOrNull()?.message ?: "Registration failed")
    }

    fun logout() = viewModelScope.launch { repo.logout() }

    fun resetState() { _state.value = State.Idle }

    private fun launch(block: suspend () -> Unit) = viewModelScope.launch {
        _state.value = State.Loading
        block()
    }
}
