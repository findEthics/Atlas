package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class PerplexityResponse(
    val choices: List<Choice>
)
