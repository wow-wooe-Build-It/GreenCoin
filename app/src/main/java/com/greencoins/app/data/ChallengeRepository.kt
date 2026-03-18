package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChallengeRepository {
    private val client = SupabaseManager.client

    // Fetch active challenges
    suspend fun getChallenges(): List<Challenge> = withContext(Dispatchers.IO) {
        try {
            client.from("challenges").select {
                filter {
                    eq("is_active", true)
                }
            }.decodeList<Challenge>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    // Fetch all challenges (including featured/global which we map from same table for now)
    suspend fun getAllChallenges(): List<Challenge> = withContext(Dispatchers.IO) {
        try {
            client.from("challenges").select().decodeList<Challenge>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
