package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class MessageContent(
    val role: String,
    val content: String
)
