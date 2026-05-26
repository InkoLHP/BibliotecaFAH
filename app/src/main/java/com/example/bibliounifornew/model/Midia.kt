package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class Midia(
    val id: Int? = null,
    val titulo: String,
    val autor: String,
    val isbn: String
)