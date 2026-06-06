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
    val created_at: String? = null,
    // 🌟 Novos campos adicionados para gerenciar os convites de amizade
    @SerialName("tipo")
    val tipo: String? = "aviso", // Define se é um "aviso" comum ou "convite_amizade"
    @SerialName("remetente_email")
    val remetente_email: String? = null // Guarda quem enviou o convite para poder aceitar depois
)