package com.example.bibliounifornew.model // Organizando no pacote model

import kotlinx.serialization.Serializable

@Serializable
data class Livro(
    val id: String? = null, // ID do Supabase (pode ser Int se sua coluna for int8)
    val titulo: String,
    val autor: String,
    val isbn: String,
    val sinopse: String? = null,     // Adicionado para usar na TelaRF12
    val capaResourceId: Int = 0      // Adicionado para usar na TelaRF12
)