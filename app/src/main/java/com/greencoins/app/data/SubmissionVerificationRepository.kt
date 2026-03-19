package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.functions.functions
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class VerifySubmissionRequest(
    val submissionId: String,
    val beforeUrl: String,
    val afterUrl: String,
    val mission: String
)

@Serializable
data class VerifySubmissionResponse(
    val success: Boolean,
    val updated: Boolean = false,
    val finalScore: Double? = null,
    val ai_probability: Double? = null,
    val rejected_reason: String? = null
)

object SubmissionVerificationRepository {
    private val client = SupabaseManager.client

    // Invoke the AI Verification Edge Function
    suspend fun verifySubmission(
        submissionId: String,
        beforeUrl: String,
        afterUrl: String,
        missionTitle: String
    ): VerifySubmissionResponse? = withContext(Dispatchers.IO) {
        try {
            val reqBody = buildJsonObject {
                put("submissionId", submissionId)
                put("beforeUrl", beforeUrl)
                put("afterUrl", afterUrl)
                put("mission", missionTitle)
            }.toString()

            val responseStr = client.functions.invoke("verify-submission") {
                setBody(reqBody)
            }.bodyAsText()

            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString(
                VerifySubmissionResponse.serializer(), responseStr
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Refetch the canonical submission row exactly as recorded in the DB
    suspend fun getSubmissionById(id: String): Submission? = withContext(Dispatchers.IO) {
        try {
            client.from("submissions").select {
                filter { eq("id", id) }
            }.decodeSingleOrNull<Submission>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
