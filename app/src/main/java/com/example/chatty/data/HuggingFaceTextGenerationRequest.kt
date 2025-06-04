package com.example.chatty.data
import kotlinx.serialization.Serializable

@Serializable
data class HuggingFaceTextGenerationRequest(
    val inputs: String,
    val parameters: HuggingFaceParameters? = null, // Optional parameters
    val options: HuggingFaceOptions? = HuggingFaceOptions() // Ensure wait_for_model is true
)
