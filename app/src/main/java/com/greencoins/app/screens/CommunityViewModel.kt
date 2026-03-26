package com.greencoins.app.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.CommunityRepository
import com.greencoins.app.data.CommunitySubmission
import com.greencoins.app.data.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private companion object {
        private const val TAG = "CommunityViewModel"
    }
    private val _submissions = MutableStateFlow<List<CommunitySubmission>>(emptyList())
    val submissions: StateFlow<List<CommunitySubmission>> = _submissions.asStateFlow()

    private val _commentsBySubmission = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val commentsBySubmission: StateFlow<Map<String, List<Comment>>> = _commentsBySubmission.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        refreshSubmissions()
    }

    fun vote(submissionId: String, type: String) {
        val userId = AuthRepository.currentUser?.id ?: return
        viewModelScope.launch {
            val snapshot = _submissions.value
            _submissions.value = snapshot.map { applyVoteOptimistic(it, submissionId, userId, type) }
            val ok = CommunityRepository.submitVote(submissionId, userId, type)
            if (!ok) _submissions.value = snapshot
            refreshSubmissions()
        }
    }

    private fun applyVoteOptimistic(
        s: CommunitySubmission,
        submissionId: String,
        userId: String,
        type: String,
    ): CommunitySubmission {
        if (s.id != submissionId) return s
        val oldVote = s.votesBy[userId]
        var up = s.upvotesOverride ?: s.upvotes
        var down = s.downvotesOverride ?: s.downvotes
        if (oldVote == "upvote") up--
        if (oldVote == "downvote") down--
        when (type) {
            "upvote" -> up++
            "downvote" -> down++
        }
        return s.copy(
            votesBy = mapOf(userId to type),
            upvotesOverride = up,
            downvotesOverride = down,
        )
    }

    fun addComment(submissionId: String, text: String) {
        val userId = AuthRepository.currentUser?.id ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            CommunityRepository.addComment(submissionId, userId, trimmed)
            loadComments(submissionId)
        }
    }

    fun refreshSubmissions() {
        viewModelScope.launch {
            val userId = AuthRepository.currentUser?.id
            if (userId == null) {
                _isLoading.value = false
                _submissions.value = emptyList()
                return@launch
            }
            _isLoading.value = true
            try {
                val dtos = CommunityRepository.getSubmissions()
                val ids = dtos.map { it.id }
                val voteCounts = CommunityRepository.getVoteCountsForSubmissions(ids, userId)
                _submissions.value = dtos.map { dto ->
                    CommunityRepository.toCommunitySubmission(dto, voteCounts[dto.id], userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "refreshSubmissions failed", e)
                _submissions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadComments(submissionId: String) {
        viewModelScope.launch {
            val dtos = CommunityRepository.getCommentsBySubmission(submissionId)
            val comments = dtos.map { Comment.fromDto(it) }
            _commentsBySubmission.value = _commentsBySubmission.value + (submissionId to comments)
        }
    }

    /** Delete comment. Only succeeds if current user owns the comment. */
    fun deleteComment(submissionId: String, commentId: String) {
        val userId = AuthRepository.currentUser?.id ?: return
        viewModelScope.launch {
            if (CommunityRepository.deleteComment(commentId, userId)) {
                loadComments(submissionId)
            }
        }
    }
}
