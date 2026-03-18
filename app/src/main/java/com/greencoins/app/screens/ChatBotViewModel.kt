package com.greencoins.app.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.ChatRepository
import com.greencoins.app.data.Mission
import com.greencoins.app.data.MissionRepository
import com.greencoins.app.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class ChatBotViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        _messages.value = listOf(ChatMessage("Hi! I'm GreenBot 🌱. Ask me anything about your eco impact!", isUser = false))
    }

    fun sendMessage(message: String) {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return

        _messages.value = _messages.value + ChatMessage(trimmed, isUser = true)
        viewModelScope.launch {
            _isTyping.value = true
            val response = processQuery(trimmed)
            _isTyping.value = false
            _messages.value = _messages.value + ChatMessage(response, isUser = false)
        }
    }

    fun sendSuggestion(text: String) {
        sendMessage(text)
    }

    private suspend fun processQuery(query: String): String {
        val lower = query.lowercase()
        val userId = AuthRepository.currentUser?.id

        if (userId != null) {
            when {
                lower.contains("coin") -> {
                    val coins = UserRepository.getTotalGc(userId)
                    return "You have $coins GreenCoins! 🪙 Spend them in the Shop or keep earning with missions."
                }
                lower.contains("streak") -> {
                    val streak = UserRepository.getCalculatedStreak(userId)
                    return if (streak > 0) {
                        "Your current streak is $streak day${if (streak == 1) "" else "s"}! 🔥 Keep it up!"
                    } else {
                        "Your streak is 0. Complete a mission today to start a new streak!"
                    }
                }
                lower.contains("mission") -> {
                    val missions = MissionRepository.getMissions()
                    return formatMissionsResponse(missions)
                }
            }
        }

        return ChatRepository.getResponse(query)
    }

    private fun formatMissionsResponse(missions: List<Mission>): String {
        if (missions.isEmpty()) return "No missions available right now. Check back later!"
        val top = missions.take(5)
        val lines = top.mapIndexed { i, m -> "${i + 1}. ${m.title} (+${m.gcReward} GC)" }
        return "Here are available missions:\n\n${lines.joinToString("\n")}\n\nGo to the Plus tab to start one!"
    }
}
