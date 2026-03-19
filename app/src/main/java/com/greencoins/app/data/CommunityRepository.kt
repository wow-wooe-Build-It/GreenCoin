package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** RPC response: get_vote_counts row. */
@Serializable
data class VoteCountRow(
    @SerialName("submission_id") val submissionId: String,
    val upvotes: Long,
    val downvotes: Long,
)

/** RPC response: get_my_votes row. */
@Serializable
data class MyVoteRow(
    @SerialName("submission_id") val submissionId: String,
    val vote: String,
)

/** RPC response: get_comments_with_user row. */
@Serializable
data class CommentResultRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String? = null,
    val comment: String,
    @SerialName("created_at") val createdAt: String,
)

/** Payload for upserting a vote. */
@Serializable
data class CommunityVoteUpsert(
    @SerialName("user_id") val userId: String,
    @SerialName("submission_id") val submissionId: String,
    @SerialName("vote_type") val voteType: String, // "upvote" | "downvote"
)

/** Raw vote row from community_votes. */
@Serializable
data class CommunityVoteRow(
    @SerialName("submission_id") val submissionId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("vote_type") val voteType: String,
)

/** Vote counts and current user's vote for a submission. */
data class VoteCounts(
    val submissionId: String,
    val upvotes: Int,
    val downvotes: Int,
    val currentUserVote: String?, // "upvote" | "downvote" | null
)

/** Comment row from comments table with user info. */
@Serializable
data class CommentDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("submission_id") val submissionId: String,
    val comment: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("user_name") val userName: String? = null,
)

/** UI model for comment display (userName non-null). */
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
            comment = dto.comment,
            createdAt = dto.createdAt,
        )
    }
}

@Serializable
data class MissionSnippet(
    val title: String,
    @SerialName("gc_reward") val gcReward: Int
)

/** UI model for Community Verification cards - maps from backend submissions. */
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
    private val client = SupabaseManager.client

    /** Fetch ALL submissions for Community Verification, ordered by newest first. */
    suspend fun getAllSubmissions(currentUserId: String): List<CommunitySubmissionDto> = withContext(Dispatchers.IO) {
        try {
            client.from("submissions")
                .select(Columns.raw("id, mission_id, user_id, before_image_url, after_image_url, location_name, description, status, latitude, longitude, created_at, missions(title, gc_reward)")) {
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<CommunitySubmissionDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Map DTO to UI model for Community Verification screen. */
    fun toCommunitySubmission(dto: CommunitySubmissionDto): CommunitySubmission =
        toCommunitySubmission(dto, null, null)

    /** Map DTO to UI model with vote counts. */
    fun toCommunitySubmission(dto: CommunitySubmissionDto, voteCounts: VoteCounts?, currentUserId: String?): CommunitySubmission {
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

    /** Vote (upsert). Alias for upsertVote. */
    suspend fun vote(submissionId: String, userId: String, type: String): Boolean =
        upsertVote(userId, submissionId, type)

    /** Add comment. Alias for insertComment. */
    suspend fun addComment(submissionId: String, userId: String, text: String): Boolean =
        insertComment(userId, submissionId, text)

    /** Add or update vote (upsert). Uses RPC for reliable ON CONFLICT behavior. */
    suspend fun upsertVote(userId: String, submissionId: String, voteType: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest.rpc(
                "upsert_vote",
                buildJsonObject {
                    put("p_user_id", userId)
                    put("p_submission_id", submissionId)
                    put("p_vote_type", voteType)
                }
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Fetch vote counts per submission and current user's vote. Uses RPC functions. */
    suspend fun getVoteCountsForSubmissions(submissionIds: List<String>, currentUserId: String?): Map<String, VoteCounts> = withContext(Dispatchers.IO) {
        if (submissionIds.isEmpty()) return@withContext emptyMap()
        try {
            val countRows = client.postgrest.rpc("get_vote_counts").decodeList<VoteCountRow>()
            val countMap = countRows.associate { it.submissionId to it }

            val myVotes = currentUserId?.let { uid ->
                client.postgrest.rpc(
                    "get_my_votes",
                    buildJsonObject { put("p_user_id", uid) }
                ).decodeList<MyVoteRow>().associate { it.submissionId to it.vote }
            } ?: emptyMap()

            submissionIds.associateWith { subId ->
                val row = countMap[subId]
                val upvotes = (row?.upvotes ?: 0L).toInt()
                val downvotes = (row?.downvotes ?: 0L).toInt()
                val userVote = myVotes[subId]
                VoteCounts(submissionId = subId, upvotes = upvotes, downvotes = downvotes, currentUserVote = userVote)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            submissionIds.associateWith { VoteCounts(it, 0, 0, null) }
        }
    }

    /** Insert a new comment. Uses RPC for reliable insert. */
    suspend fun insertComment(userId: String, submissionId: String, comment: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest.rpc(
                "add_comment",
                buildJsonObject {
                    put("p_user_id", userId)
                    put("p_submission_id", submissionId)
                    put("p_comment", comment)
                }
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Fetch comments by submission_id with user info, ordered by created_at DESC. Uses RPC. */
    suspend fun getCommentsBySubmission(submissionId: String): List<CommentDto> = withContext(Dispatchers.IO) {
        try {
            val rows = client.postgrest.rpc(
                "get_comments_with_user",
                buildJsonObject { put("p_submission_id", submissionId) }
            ).decodeList<CommentResultRow>()
            rows.map { CommentDto(it.id, it.userId, submissionId, it.comment, it.createdAt, it.userName) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Delete comment. Only succeeds if user_id matches (ownership check). */
    suspend fun deleteComment(commentId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest.rpc(
                "delete_comment",
                buildJsonObject {
                    put("p_comment_id", commentId)
                    put("p_user_id", userId)
                }
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** @deprecated Use upsertVote instead. Kept for backward compatibility. */
    suspend fun submitVote(submissionId: String, userId: String, vote: String, reason: String? = null): Boolean =
        upsertVote(userId, submissionId, vote)
}
