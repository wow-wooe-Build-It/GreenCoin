package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class MissionSnippet(
    val title: String,
    @SerialName("gc_reward") val gcReward: Int
)

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

    suspend fun getNearbyPendingSubmissions(currentUserId: String): List<CommunitySubmissionDto> = withContext(Dispatchers.IO) {
        try {
            // A production app would use PostGIS for true radius filtering.
            // For now, we fetch all pending ones skipping the user's own submissions.
            val allPending = client.from("submissions")
                .select(Columns.raw("id, mission_id, user_id, before_image_url, after_image_url, location_name, description, status, latitude, longitude, created_at, missions(title, gc_reward)")) {
                    filter {
                        eq("status", "pending")
                        neq("user_id", currentUserId)
                    }
                }
                .decodeList<CommunitySubmissionDto>()
            
            // Exclude items the user has already voted on
            val votedIds = getUserVotedSubmissionIds(currentUserId)
            
            allPending.filter { it.id !in votedIds }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun submitVote(submissionId: String, userId: String, vote: String, reason: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val voteJson = buildJsonObject {
                put("submission_id", submissionId)
                put("user_id", userId)
                put("vote", vote)
                if (!reason.isNullOrBlank()) {
                    put("reason", reason)
                }
            }
            client.from("community_votes").insert(voteJson)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // Get the list of submission IDs the current user has already voted on
    private suspend fun getUserVotedSubmissionIds(userId: String): Set<String> {
        return try {
            @Serializable
            data class VoteRow(val submission_id: String)
            
            client.from("community_votes")
                .select(Columns.raw("submission_id")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<VoteRow>()
                .map { it.submission_id }
                .toSet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }
}
