package com.example.chatty.data
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class HuggingFaceParameters(
    @SerialName("max_new_tokens")
    val maxNewTokens: Int = 250, // Example parameter
    @SerialName("return_full_text")
    val returnFullText: Boolean = false // Usually false to get only the generated part
    // Add other parameters as needed by your chosen model
)
