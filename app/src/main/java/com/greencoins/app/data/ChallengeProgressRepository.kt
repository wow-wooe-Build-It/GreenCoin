package com.greencoins.app.data

import android.util.Log
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.ZoneId

@Serializable
data class ChallengeParticipation(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("coins_earned") val coinsEarned: Int = 0,
    val progress: Int = 0,
    val streak: Int = 0,
    @SerialName("last_active_date") val lastActiveDate: String? = null,
    @SerialName("joined_at") val joinedAt: String? = null,
)

@Serializable
private data class ChallengeParticipationInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("coins_earned") val coinsEarned: Int = 0,
    val progress: Int = 0,
    val streak: Int = 0,
)

object ChallengeProgressRepository {
    private const val TAG = "ChallengeProgressRepository"

    private val client = SupabaseManager.client
    private val zone: ZoneId get() = ZoneId.systemDefault()

    suspend fun joinChallenge(userId: String, challengeId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("challenge_participation").insert(
                ChallengeParticipationInsert(
                    userId = userId,
                    challengeId = challengeId,
                    coinsEarned = 0,
                    progress = 0,
                    streak = 0,
                )
            )
            true
        } catch (e: Exception) {
            // Idempotent: duplicate join (unique constraint) is OK
            if (e.message?.contains("duplicate", ignoreCase = true) == true ||
                e.message?.contains("23505", ignoreCase = true) == true
            ) {
                true
            } else {
                Log.e(TAG, "joinChallenge failed", e)
                false
            }
        }
    }

    suspend fun getProgress(userId: String, challengeId: String): ChallengeParticipation? = withContext(Dispatchers.IO) {
        try {
            client.from("challenge_participation").select {
                filter {
                    eq("user_id", userId)
                    eq("challenge_id", challengeId)
                }
            }.decodeList<ChallengeParticipation>().firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "getProgress failed", e)
            null
        }
    }

    suspend fun getAllParticipation(userId: String): List<ChallengeParticipation> = withContext(Dispatchers.IO) {
        try {
            client.from("challenge_participation").select {
                filter { eq("user_id", userId) }
            }.decodeList<ChallengeParticipation>()
        } catch (e: Exception) {
            Log.e(TAG, "getAllParticipation failed", e)
            emptyList()
        }
    }

    /**
     * Adds [coinsDelta] to [coins_earned], increments [progress] by 1, and updates streak from [last_active_date].
     * Returns false if the user has no participation row for this challenge.
     */
    suspend fun updateProgress(userId: String, challengeId: String, coinsDelta: Int): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val row = getProgress(userId, challengeId) ?: return@withContext false
                val today = LocalDate.now(zone)
                val last = row.lastActiveDate?.let { parseLocalDate(it) }

                val newCoins = row.coinsEarned + coinsDelta
                val newProgress = row.progress + 1

                val newStreak: Int
                val newLastActive: String?
                when {
                    last == null -> {
                        newStreak = 1
                        newLastActive = today.toString()
                    }
                    last == today -> {
                        newStreak = row.streak
                        newLastActive = row.lastActiveDate
                    }
                    last == today.minusDays(1) -> {
                        newStreak = row.streak + 1
                        newLastActive = today.toString()
                    }
                    else -> {
                        newStreak = 1
                        newLastActive = today.toString()
                    }
                }

                client.from("challenge_participation").update(
                    mapOf(
                        "coins_earned" to newCoins,
                        "progress" to newProgress,
                        "streak" to newStreak,
                        "last_active_date" to newLastActive,
                    )
                ) {
                    filter { eq("id", row.id) }
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "updateProgress failed", e)
                false
            }
        }

    private fun parseLocalDate(value: String): LocalDate? =
        try {
            LocalDate.parse(value.take(10))
        } catch (_: Exception) {
            null
        }
}
