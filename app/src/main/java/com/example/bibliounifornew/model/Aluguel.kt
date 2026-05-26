package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class Aluguel(
    val id: Int? = null,
    val email_usuario: String,
    val titulo_livro: String,
    val autor_livro: String,
    val capa_url: String? = null,
    val data_vencimento: String,
    val dias_restantes: Int,
    val devolvido: Boolean = false,
    val oculto_historico: Boolean = false
) : java.io.Serializable