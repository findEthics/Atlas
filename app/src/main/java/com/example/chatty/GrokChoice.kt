package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class GrokChoice(
    val message: MessageContent
)
