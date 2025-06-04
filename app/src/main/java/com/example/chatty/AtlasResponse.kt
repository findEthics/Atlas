package com.example.chatty

import kotlinx.serialization.Serializable

@Serializable
data class AtlasResponse(
    val response: String,
    val search_results: List<Map<String, String>>? = null
)