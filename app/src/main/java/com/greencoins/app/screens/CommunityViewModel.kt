package com.greencoins.app.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencoins.app.data.AuthRepository
import com.greencoins.app.data.CommunityRepository
import com.greencoins.app.data.CommunitySubmission
import com.greencoins.app.data.CommunitySubmissionDto
import com.greencoins.app.data.Comment
import com.greencoins.app.data.CommentDto
import com.greencoins.app.data.VoteCounts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
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
            CommunityRepository.vote(submissionId, userId, type)
            refreshSubmissions()
        }
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
        val userId = AuthRepository.currentUser?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val dtos = CommunityRepository.getAllSubmissions(userId)
            val ids = dtos.map { it.id }
            val voteCounts = CommunityRepository.getVoteCountsForSubmissions(ids, userId)
            _submissions.value = dtos.map { dto ->
                CommunityRepository.toCommunitySubmission(dto, voteCounts[dto.id], userId)
            }
            _isLoading.value = false
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
