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
private data class UserChallengeRow(
    @SerialName("challenge_id") val challengeId: String,
)

object UserChallengesRepository {
    private val client = SupabaseManager.client

    suspend fun getJoinedChallengeIds(userId: String): Set<String> = withContext(Dispatchers.IO) {
        try {
            val rows = client.from("user_challenges").select(columns = Columns.list("challenge_id")) {
                filter { eq("user_id", userId) }
            }.decodeList<UserChallengeRow>()
            rows.map { it.challengeId }.toSet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }

    suspend fun joinChallenge(userId: String, challengeId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = buildJsonObject {
                put("user_id", userId)
                put("challenge_id", challengeId)
                put("challenge_score", 0)
            }
            client.from("user_challenges").insert(json)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
