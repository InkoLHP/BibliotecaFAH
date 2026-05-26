package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class Notificacao(
    val id: Int? = null,
    val titulo: String,
    val mensagem: String,
    val visualizada: Boolean = false,
    val created_at: String? = null
)