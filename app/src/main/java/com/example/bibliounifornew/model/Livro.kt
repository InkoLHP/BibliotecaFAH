package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class Livro(
    val id: String? = null,
    val titulo: String,
    val autor: String,
    val isbn: String,
    val capaUrl: String? = null,
    val sinopse: String? = null,
    val data_publicacao: String? = null,
    val categoria: String? = null,
    val formato: String? = null,
    val disponível: Boolean = true
) : java.io.Serializable