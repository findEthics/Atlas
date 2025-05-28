package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    val message: MessageContent
)
