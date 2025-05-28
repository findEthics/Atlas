package com.example.chatty

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

class PerplexityClient {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun query(prompt: String, apiKey: String): String {
        val response = client.post("https://api.perplexity.ai/chat/completions") {
            headers {
                append("Authorization", "Bearer $apiKey")
            }
            contentType(ContentType.Application.Json)
            setBody(PerplexityRequest(
                model = "sonar-pro",
                messages = listOf(MessageContent("user", prompt))
            ))
        }
        return when (response.status.value) {
            in 200..299 -> response.body<PerplexityResponse>().choices.first().message.content
            else -> throw Exception("API request failed: ${response.status}")
        }
    }

    fun loadApiKey(context: Context): String {
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

        return prefs.getString("api_key", "") ?: ""
    }

    @Composable
    fun promptApiKey(context: Context): String? =
        CoroutineScope(Dispatchers.Main).run {
            var apiKey by mutableStateOf("")
            var showDialog by mutableStateOf(true)

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Enter Perplexity API Key") },
                    text = {
                        TextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("API Key") }
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            saveApiKey(context, apiKey)
                        }) {
                            Text("Save")
                        }
                    }
                )
            }
            apiKey.takeIf { it.isNotEmpty() }
        }

    fun saveApiKey(context: Context, key: String) {
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

        prefs.edit().putString("api_key", key).apply()
    }
}
