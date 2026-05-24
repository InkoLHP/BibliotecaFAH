package com.example.bibliounifornew.usuario

import kotlinx.serialization.Serializable

// data class dos livros, representa os dados vindo da tabela livros do supabase

@Serializable
data class Livro(
    val id: String? = null,
    val titulo: String,
    val autor: String,
    val isbn: String,
    // Adicionamos a URL da capa que virá do Supabase
    val capaUrl: String? = null,

    val sinopse: String? = null,
    val data_publicacao: String? = null,
    val categoria: String? = null,
    val formato: String? = null,
    val disponível: Boolean = true
)