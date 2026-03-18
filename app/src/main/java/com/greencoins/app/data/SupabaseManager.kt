package com.greencoins.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.greencoins.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.encodeToString
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

object SupabaseManager {
    lateinit var client: SupabaseClient
        private set

    fun initialize(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "supabase_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth) {
                scheme = "greencoins"
                host = "login"
                sessionManager = object : SessionManager {
                    override suspend fun saveSession(session: UserSession) {
                        val json = Json.encodeToString(session)
                        sharedPreferences.edit().putString("session", json).apply()
                    }

                    override suspend fun loadSession(): UserSession? {
                        val json = sharedPreferences.getString("session", null) ?: return null
                        return try {
                            Json.decodeFromString(json)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    override suspend fun deleteSession() {
                        sharedPreferences.edit().remove("session").apply()
                    }
                }
            }
            install(Storage)
            install(Postgrest)
        }
    }

    fun handleDeepLink(intent: android.content.Intent) {
        client.handleDeeplinks(intent)
    }
}
