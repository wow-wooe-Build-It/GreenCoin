package com.greencoins.app.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.Google
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

import io.github.jan.supabase.auth.user.UserInfo

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AuthRepository {
    private val auth = SupabaseManager.client.auth
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        scope.launch {
            auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _isLoggedIn.value = true
                        _currentUserId.value = auth.currentUserOrNull()?.id
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _isLoggedIn.value = false
                        _currentUserId.value = null
                    }
                    is SessionStatus.Initializing -> { /* Wait for preferences */ }
                    is SessionStatus.RefreshFailure -> {
                        _isLoggedIn.value = false
                        _currentUserId.value = null
                    }
                }
            }
        }
    }

    val currentUser: UserInfo?
        get() = auth.currentUserOrNull()

    suspend fun signUpWithEmail(email: String, pass: String, name: String, phone: String) {
        auth.signUpWith(Email) {
            this.email = email
            password = pass
            data = buildJsonObject {
                put("full_name", name)
                put("phone", phone)
            }
        }
    }

    suspend fun signInWithEmail(email: String, pass: String) {
        auth.signInWith(Email) {
            this.email = email
            password = pass
        }
    }

    suspend fun signInWithGoogle() {
        auth.signInWith(Google)
    }
    
    fun isUserLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }
    
    suspend fun logout() {
        auth.signOut()
    }
}
