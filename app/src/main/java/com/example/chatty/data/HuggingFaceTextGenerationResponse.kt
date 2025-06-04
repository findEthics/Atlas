package com.example.chatty.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class HuggingFaceTextGenerationResponse(
    @SerialName("generated_text")
    val generatedText: String
)
