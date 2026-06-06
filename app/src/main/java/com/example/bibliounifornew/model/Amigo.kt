package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class Amigo(
    val usuario_email: String,
    val amigo_email: String
)