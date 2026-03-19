package com.greencoins.app.data

import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object SubmissionVerificationRepository {
    private val client = SupabaseManager.client

    suspend fun triggerVerification(submissionId: String, beforeUrl: String, afterUrl: String, mission: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = buildJsonObject {
                put("submissionId", submissionId)
                put("beforeUrl", beforeUrl)
                put("afterUrl", afterUrl)
                put("mission", mission)
            }
            client.functions.invoke("verify-submission", body)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getSubmissionById(submissionId: String): Submission? = withContext(Dispatchers.IO) {
        try {
            client.from("submissions").select {
                filter {
                    eq("id", submissionId)
                }
            }.decodeSingle<Submission>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
