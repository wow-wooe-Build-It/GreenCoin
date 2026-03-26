package com.greencoins.app.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencoins.app.data.Challenge
import com.greencoins.app.data.ChallengesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChallengesViewModel : ViewModel() {
    private val _challenges = MutableStateFlow<List<Challenge>>(emptyList())
    val challenges: StateFlow<List<Challenge>> = _challenges.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadChallenges()
    }

    fun loadChallenges() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _challenges.value = ChallengesRepository.getChallenges()
            } catch (e: Exception) {
                Log.e(TAG, "loadChallenges failed", e)
                _challenges.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private companion object {
        private const val TAG = "ChallengesViewModel"
    }
}
