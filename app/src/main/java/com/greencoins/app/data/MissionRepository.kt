package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MissionRepository {
    private val client = SupabaseManager.client

    // Fetch all missions
    suspend fun getMissions(): List<Mission> = withContext(Dispatchers.IO) {
        try {
            client.from("missions").select().decodeList<Mission>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getMission(id: String): Mission? = withContext(Dispatchers.IO) {
        try {
            client.from("missions").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingle<Mission>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadMissionProof(userId: String, imageBytes: ByteArray, suffix: String = ""): String = withContext(Dispatchers.IO) {
         val baseName = "${System.currentTimeMillis()}"
         val fileName = if (suffix.isNotEmpty()) "$userId/${baseName}_$suffix.jpg" else "$userId/$baseName.jpg"
         val bucket = client.storage.from("mission-proofs")
         bucket.upload(fileName, imageBytes) {
             upsert = false
         }
         bucket.publicUrl(fileName)
    }


    // Submit a proof (Create submission)
    suspend fun submitMission(userId: String, missionId: String, beforeImageUrl: String?, afterImageUrl: String?, description: String?) = withContext(Dispatchers.IO) {
        val submissionJson = buildJsonObject {
            put("user_id", userId)
            put("mission_id", missionId)
            put("before_image_url", beforeImageUrl)
            put("after_image_url", afterImageUrl)
            put("image_url", afterImageUrl) // Keep for backward compatibility
            put("description", description)
            put("status", "pending")
        }
        
        client.from("submissions").insert(submissionJson)
        Unit
    }
}
