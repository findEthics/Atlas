package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class GrokRequest(
    val model: String,
    val messages: List<MessageContent>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1000
)
