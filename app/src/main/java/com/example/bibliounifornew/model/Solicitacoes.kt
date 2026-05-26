package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class Solicitacao(
    val id: Int? = null,
    val titulo: String,
    val autor: String,
    val email_usuario: String = "",
    val usuario_nome: String = "", // Adicionado para corrigir o erro do adapter
    val tipo_solicitacao: String,
    val capa_url: String? = null,
    val status: String
)