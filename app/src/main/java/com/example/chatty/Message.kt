package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class Message(val query: String, val response: String)
