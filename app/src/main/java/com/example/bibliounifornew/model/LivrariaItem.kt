package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class LivrariaItem(
    val id: Int? = null,
    val email_usuario: String,
    val livro_id: Int? = null,
    val titulo: String,
    val autor: String,
    val capa_url: String? = null,
    val categoria: String? = null
) : java.io.Serializable