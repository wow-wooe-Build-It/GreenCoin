package com.greencoins.app.data

import android.util.Log
import com.greencoins.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.*
import java.util.concurrent.TimeUnit

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object ChatRepository {

    private const val TAG = "ChatRepository"

    private const val OUT_OF_CONTEXT_RESPONSE = "This is out of context. I can only help with eco-related queries 🌱"

    private val BLOCKED_KEYWORDS = listOf(
        "code", "c++", "java", "python", "algorithm", "movie", "song", "cricket"
    )

    private val SYSTEM_PROMPT = """
You are GreenBot, an eco assistant for the GreenCoins app.

You ONLY answer questions related to:
- sustainability
- environment
- eco-friendly actions
- recycling, planting trees, cleanups
- GreenCoins missions, rewards, coins, streaks

STRICT RULES:
- If the question is NOT related to environment or GreenCoins, DO NOT answer it.
- Instead reply: 'This is out of context. I can only help with eco-related queries 🌱'
- Do NOT give coding help, technical answers, or general knowledge outside eco topics.
""".trimIndent()

    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key="

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getResponse(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            val lowerMessage = userMessage.lowercase()
            if (BLOCKED_KEYWORDS.any { lowerMessage.contains(it) }) {
                return@withContext OUT_OF_CONTEXT_RESPONSE
            }

            val apiKey = BuildConfig.OPENAI_API_KEY

            if (apiKey.isBlank()) {
                return@withContext "API key missing"
            }

            val url = ENDPOINT + apiKey

            val requestJson = buildJsonObject {
                put("systemInstruction", buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", SYSTEM_PROMPT)
                        })
                    })
                })
                put("contents", buildJsonArray {
                    add(buildJsonObject {
                        put("parts", buildJsonArray {
                            add(buildJsonObject {
                                put("text", userMessage)
                            })
                        })
                    })
                })
            }

            val requestBody = requestJson.toString()

            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "CODE: ${response.code}")
            Log.d(TAG, "BODY: $responseBody")

            if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                return@withContext "Error: ${response.code}"
            }

            val json = Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(responseBody).jsonObject

            val text = root["candidates"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")?.jsonPrimitive?.content

            return@withContext text ?: "No response"

        } catch (e: Exception) {
            Log.e(TAG, "ERROR", e)
            return@withContext "Something went wrong"
        }
    }
}