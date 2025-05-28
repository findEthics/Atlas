package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class GrokResponse(
    val choices: List<GrokChoice>
)
