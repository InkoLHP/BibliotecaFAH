package com.example.bibliounifornew.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notificacao(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("email_usuario")
    val email_usuario: String = "",
    @SerialName("titulo")
    val titulo: String = "",
    @SerialName("mensagem")
    val mensagem: String = "",
    @SerialName("visualizada")
    val visualizada: Boolean = false,
    @SerialName("created_at")
    val created_at: String? = null
)