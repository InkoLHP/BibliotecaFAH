package com.example.bibliounifornew.usuario

import kotlinx.serialization.Serializable

// data class dos aluguel, representa os dados vindo da tabela alugues do supabase
@Serializable
data class Aluguel(

    val id: Int? = null,

    val email_usuario: String? = null,

    val titulo_livro: String? = null,

    val autor_livro: String? = null,

    val capa_url: String? = null,

    val data_vencimento: String? = null,

    val dias_restantes: Int? = null,

    val devolvido: Boolean = false
)