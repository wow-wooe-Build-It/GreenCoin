package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class VerificationStatusRow(val status: String)

object VerificationRepository {
    private val client = SupabaseManager.client

    suspend fun uploadVerificationImage(userId: String, imageBytes: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "$userId/${System.currentTimeMillis()}.jpg"
        val bucket = client.storage.from("verification-images")
        bucket.upload(fileName, imageBytes) { upsert = false }
        bucket.publicUrl(fileName)
    }

    suspend fun insertVerificationRequest(
        submissionId: String,
        userId: String,
        reason: String,
        imageUrl: String?,
    ) = withContext(Dispatchers.IO) {
        val json = buildJsonObject {
            put("submission_id", submissionId)
            put("user_id", userId)
            put("reason", reason)
            put("image_url", imageUrl)
            put("status", "pending")
        }
        client.from("verification_requests").insert(json)
    }

    suspend fun getVerificationStatus(userId: String, submissionId: String): String? = withContext(Dispatchers.IO) {
        try {
            val rows = client.from("verification_requests").select(Columns.list("status")) {
                filter {
                    eq("user_id", userId)
                    eq("submission_id", submissionId)
                }
            }.decodeList<VerificationStatusRow>()
            rows.firstOrNull()?.status
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
