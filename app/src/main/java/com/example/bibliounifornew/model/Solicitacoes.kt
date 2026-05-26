package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class Solicitacao(
    val id: Int? = null,
    val titulo: String,
    val autor: String,
    val email_usuario: String,
    val tipo_solicitacao: String,
    val capa_url: String?,
    val status: String
)