package com.greencoins.app.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.Challenge
import com.greencoins.app.data.ChallengeParticipation
import com.greencoins.app.data.ChallengeProgressRepository
import com.greencoins.app.data.ChallengesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChallengesViewModel : ViewModel() {
    private val _challenges = MutableStateFlow<List<Challenge>>(emptyList())
    val challenges: StateFlow<List<Challenge>> = _challenges.asStateFlow()

    /** Per-challenge participation for the signed-in user (empty if logged out). */
    private val _participationByChallengeId = MutableStateFlow<Map<String, ChallengeParticipation>>(emptyMap())
    val participationByChallengeId: StateFlow<Map<String, ChallengeParticipation>> =
        _participationByChallengeId.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Reload challenges and participation (called from Challenges screen on each visit). */
    fun refresh() {
        loadChallenges()
    }

    fun loadChallenges() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _challenges.value = ChallengesRepository.getChallenges()
                val uid = AuthRepository.currentUser?.id
                _participationByChallengeId.value = if (uid != null) {
                    ChallengeProgressRepository.getAllParticipation(uid).associateBy { it.challengeId }
                } else {
                    emptyMap()
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadChallenges failed", e)
                _challenges.value = emptyList()
                _participationByChallengeId.value = emptyMap()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private companion object {
        private const val TAG = "ChallengesViewModel"
    }
}
