package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class Solicitacao(
    val id: Int? = null,
    val titulo: String,
    val autor: String,
    val usuario_nome: String,
    val tipo_solicitacao: String,
    val capa_url: String? = null,
    val status: String = "Pendente"
)