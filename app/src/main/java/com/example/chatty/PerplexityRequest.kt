package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class PerplexityRequest(
    val model: String,
    val messages: List<MessageContent>
)
