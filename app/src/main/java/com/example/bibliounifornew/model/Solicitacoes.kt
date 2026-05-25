package com.example.bibliounifornew.model

data class Solicitacao(
    val id: Int,
    val titulo: String,
    val autor: String,
    val usuario_nome: String,
    val tipo_solicitacao: String,
    val capa_url: String?,
    val status: String
)