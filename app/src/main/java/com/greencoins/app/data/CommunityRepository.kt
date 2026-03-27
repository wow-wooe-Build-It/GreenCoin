package com.greencoins.app.data

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoteRecord(
    val id: String,
    @SerialName("submission_id") val submissionId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("vote_type") val voteType: String,
)

@Serializable
data class VoteInsert(
    @SerialName("submission_id") val submissionId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("vote_type") val voteType: String,
)

@Serializable
data class CommentRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("submission_id") val submissionId: String,
    val content: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class UserNameDto(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
)

@Serializable
data class CommentInsert(
    @SerialName("submission_id") val submissionId: String,
    @SerialName("user_id") val userId: String,
    val content: String,
)

/** Vote counts and current user's vote for a submission. */
data class VoteCounts(
    val submissionId: String,
    val upvotes: Int,
    val downvotes: Int,
    val currentUserVote: String?, // "upvote" | "downvote" | null
)

/** Comment row for Community Hub (merged with display name). */
data class CommentDto(
    val id: String,
    val userId: String,
    val submissionId: String,
    val content: String,
    val createdAt: String,
    val userName: String? = null,
)

/** UI model for comment display. */
data class Comment(
    val id: String,
    val userId: String,
    val userName: String,
    val comment: String,
    val createdAt: String,
) {
    companion object {
        fun fromDto(dto: CommentDto): Comment = Comment(
            id = dto.id,
            userId = dto.userId,
            userName = dto.userName?.takeIf { it.isNotBlank() } ?: "Anonymous",
            comment = dto.content,
            createdAt = dto.createdAt,
        )
    }
}

@Serializable
data class MissionSnippet(
    val title: String,
    @SerialName("gc_reward") val gcReward: Int
)

/** UI model for Community Hub cards - maps from backend submissions. */
data class CommunitySubmission(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val status: String,
    val beforeImageUrl: String?,
    val afterImageUrl: String?,
    val votesBy: Map<String, String> = emptyMap(),
    val upvotesOverride: Int? = null,
    val downvotesOverride: Int? = null,
) {
    val upvotes: Int get() = upvotesOverride ?: votesBy.values.count { it == "upvote" }
    val downvotes: Int get() = downvotesOverride ?: votesBy.values.count { it == "downvote" }
}

@Serializable
data class CommunitySubmissionDto(
    val id: String,
    @SerialName("mission_id") val missionId: String,
    @SerialName("user_id") val submitterUserId: String,
    @SerialName("before_image_url") val beforeImageUrl: String? = null,
    @SerialName("after_image_url") val afterImageUrl: String? = null,
    @SerialName("location_name") val locationName: String? = null,
    val description: String? = null,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_at") val createdAt: String,
    val missions: MissionSnippet? = null
)

object CommunityRepository {
    private const val TAG = "CommunityRepository"

    private val client = SupabaseManager.client

    /** Fetches all submissions for Community Hub (newest first), with mission title embed. */
    suspend fun getSubmissions(): List<CommunitySubmissionDto> = withContext(Dispatchers.IO) {
        try {
            client.from("submissions")
                .select(
                    Columns.raw(
                        "id, mission_id, user_id, before_image_url, after_image_url, location_name, description, status, latitude, longitude, created_at, missions(title, gc_reward)"
                    )
                ) {
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<CommunitySubmissionDto>()
        } catch (e: Exception) {
            Log.e(TAG, "getSubmissions failed", e)
            emptyList()
        }
    }

    /** @deprecated Prefer [getSubmissions]. Parameter was unused. */
    @Deprecated("Use getSubmissions()", ReplaceWith("CommunityRepository.getSubmissions()"))
    suspend fun getAllSubmissions(currentUserId: String): List<CommunitySubmissionDto> = getSubmissions()

    /** Vote totals and current user's vote for one submission. */
    suspend fun getVotes(submissionId: String, currentUserId: String?): VoteCounts =
        getVoteCountsForSubmissions(listOf(submissionId), currentUserId)[submissionId]
            ?: VoteCounts(submissionId, 0, 0, null)

    /**
     * Loads vote rows for the given submissions and aggregates counts plus the current user's vote per submission.
     */
    suspend fun getVoteCountsForSubmissions(submissionIds: List<String>, currentUserId: String?): Map<String, VoteCounts> =
        withContext(Dispatchers.IO) {
            if (submissionIds.isEmpty()) return@withContext emptyMap()
            try {
                val rows = client.from("votes").select {
                    filter { isIn("submission_id", submissionIds) }
                }.decodeList<VoteRecord>()

                val upDownBySubmission = submissionIds.associateWith { mutableListOf<VoteRecord>() }
                for (r in rows) {
                    upDownBySubmission[r.submissionId]?.add(r)
                }

                submissionIds.associateWith { subId ->
                    val list = upDownBySubmission[subId].orEmpty()
                    var up = 0
                    var down = 0
                    var my: String? = null
                    for (v in list) {
                        when (v.voteType) {
                            "upvote" -> up++
                            "downvote" -> down++
                        }
                        if (currentUserId != null && v.userId == currentUserId) {
                            my = v.voteType
                        }
                    }
                    VoteCounts(submissionId = subId, upvotes = up, downvotes = down, currentUserVote = my)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getVoteCountsForSubmissions failed", e)
                submissionIds.associateWith { VoteCounts(it, 0, 0, null) }
            }
        }

    /** Inserts or updates the current user's vote (one vote per user per submission). */
    suspend fun submitVote(submissionId: String, userId: String, voteType: String): Boolean =
        upsertVote(userId, submissionId, voteType)

    suspend fun vote(submissionId: String, userId: String, type: String): Boolean =
        submitVote(submissionId, userId, type)

    private suspend fun upsertVote(userId: String, submissionId: String, voteType: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val existing = client.from("votes").select {
                    filter {
                        eq("submission_id", submissionId)
                        eq("user_id", userId)
                    }
                }.decodeList<VoteRecord>().firstOrNull()

                when {
                    existing == null ->
                        client.from("votes").insert(VoteInsert(submissionId, userId, voteType))
                    existing.voteType == voteType -> { /* no-op */ }
                    else ->
                        client.from("votes").update({ VoteRecord::voteType setTo voteType }) {
                            filter { eq("id", existing.id) }
                        }
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "upsertVote failed", e)
                false
            }
        }

    suspend fun addComment(submissionId: String, userId: String, text: String): Boolean =
        insertComment(userId, submissionId, text)

    private suspend fun insertComment(userId: String, submissionId: String, comment: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                client.from("comments").insert(CommentInsert(submissionId, userId, comment.trim()))
                true
            } catch (e: Exception) {
                Log.e(TAG, "insertComment failed", e)
                false
            }
        }

    suspend fun getCommentsBySubmission(submissionId: String): List<CommentDto> = withContext(Dispatchers.IO) {
        try {
            val rows = client.from("comments").select {
                filter { eq("submission_id", submissionId) }
                order(column = "created_at", order = Order.DESCENDING)
            }.decodeList<CommentRow>()
            if (rows.isEmpty()) return@withContext emptyList()
            val names = fetchUserNames(rows.map { it.userId }.distinct())
            rows.map { r ->
                CommentDto(
                    id = r.id,
                    userId = r.userId,
                    submissionId = r.submissionId,
                    content = r.content,
                    createdAt = r.createdAt,
                    userName = names[r.userId],
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCommentsBySubmission failed", e)
            emptyList()
        }
    }

    private suspend fun fetchUserNames(userIds: List<String>): Map<String, String?> = withContext(Dispatchers.IO) {
        if (userIds.isEmpty()) return@withContext emptyMap()
        try {
            val rows = client.from("users").select(columns = Columns.list("id", "full_name")) {
                filter { isIn("id", userIds) }
            }.decodeList<UserNameDto>()
            rows.associate { it.id to it.fullName }
        } catch (e: Exception) {
            Log.e(TAG, "fetchUserNames failed", e)
            emptyMap()
        }
    }

    suspend fun deleteComment(commentId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("comments").delete {
                filter {
                    and {
                        eq("id", commentId)
                        eq("user_id", userId)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteComment failed", e)
            false
        }
    }

    /** Map DTO to UI model for Community Hub screen. */
    fun toCommunitySubmission(dto: CommunitySubmissionDto): CommunitySubmission =
        toCommunitySubmission(dto, null, null)

    /** Map DTO to UI model with vote counts. */
    fun toCommunitySubmission(
        dto: CommunitySubmissionDto,
        voteCounts: VoteCounts?,
        currentUserId: String?,
    ): CommunitySubmission {
        val votesBy = when (val v = voteCounts?.currentUserVote) {
            null -> emptyMap()
            else -> currentUserId?.let { mapOf(it to v) } ?: emptyMap()
        }
        return CommunitySubmission(
            id = dto.id,
            title = dto.missions?.title ?: "Mission",
            description = dto.description ?: "",
            location = dto.locationName ?: "",
            status = dto.status,
            beforeImageUrl = dto.beforeImageUrl,
            afterImageUrl = dto.afterImageUrl,
            votesBy = votesBy,
            upvotesOverride = voteCounts?.upvotes,
            downvotesOverride = voteCounts?.downvotes,
        )
    }
}
