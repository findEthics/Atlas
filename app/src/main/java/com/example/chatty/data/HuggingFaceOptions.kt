package com.example.chatty.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HuggingFaceOptions(
    @SerialName("wait_for_model")
    val waitForModel: Boolean = true // Important for serverless inference
)
