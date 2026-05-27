package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class Solicitacao(
    val id: Long? = null,
    val titulo: String = "",
    val autor: String = "",
    val email_usuario: String = "",
    val tipo_solicitacao: String = "",
    val capa_url: String? = null,
    val status: String = ""
)