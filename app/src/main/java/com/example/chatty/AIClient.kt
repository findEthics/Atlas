package com.example.chatty

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.chatty.data.HuggingFaceParameters
import com.example.chatty.data.HuggingFaceTextGenerationRequest
import com.example.chatty.data.HuggingFaceTextGenerationResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AIClient {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun queryPerplexity(prompt: String, apiKey: String): String {
                val response = client.post("https://api.perplexity.ai/chat/completions") {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(
                        PerplexityRequest(
                            model = "sonar",
                            messages = listOf(MessageContent("user", "Answer concisely: $prompt"))
                        )
                    )
                }
                return when (response.status.value) {
                    in 200..299 -> response.body<PerplexityResponse>().choices.first().message.content
                    else -> throw Exception("API request failed: ${response.status}")
                }
            }

    suspend fun queryEthicsAtlas(prompt: String): String {

            val response = client.post("https://findEthics-Atlas.hf.space/chat") {
                contentType(ContentType.Application.Json)
                setBody(AtlasRequest(prompt = prompt, use_search = true))
            }

            return when (response.status.value) {
                in 200..299 -> response.body<AtlasResponse>().response
                else -> throw Exception("Atlas API request failed: ${response.status}")
            }
    }

    fun loadApiKey(context: Context, model: AIModel): String {
        val prefs = getEncryptedPrefs(context)
        return prefs.getString(model.name, "") ?: ""
    }

    fun saveApiKey(context: Context, key: String, model: AIModel) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().putString(model.name, key).apply()
    }

    private fun getEncryptedPrefs(context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            "api_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return prefs as EncryptedSharedPreferences
    }

}
