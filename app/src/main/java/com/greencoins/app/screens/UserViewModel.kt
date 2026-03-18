package com.greencoins.app.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Shared state for the currently logged-in user's header GC.
 * Observes auth changes and refreshes when user changes or when refresh() is called.
 */
class UserViewModel : ViewModel() {
    private val _headerCoins = MutableStateFlow(0)
    val headerCoins: StateFlow<Int> = _headerCoins.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            combine(
                AuthRepository.currentUserId,
                _refreshTrigger
            ) { userId, _ -> userId }
                .collect { userId ->
                    if (userId != null) {
                        _headerCoins.value = UserRepository.getTotalGc(userId)
                    } else {
                        _headerCoins.value = 0
                    }
                }
        }
    }

    fun refresh() {
        _refreshTrigger.value += 1
    }
}
