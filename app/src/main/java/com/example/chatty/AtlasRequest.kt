package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class AtlasRequest(
    val prompt: String,
    val max_new_tokens: Int = 500,
    val use_search: Boolean = false,
    val temperature: Double = 0.7
)
