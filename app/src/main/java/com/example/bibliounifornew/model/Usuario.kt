package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var id: Int? = null,
    var nome: String = "",       // Valor padrão evita crash se vier nulo do banco
    var email: String = "",      // Valor padrão
    var usuario: String = "",    // Valor padrão
    var senha: String = "",      // Valor padrão
    var bio: String? = null,
    var foto: String? = null,
    var tipo: String? = null,
    var credencial: String? = null,
    var created_at: String? = null // Adicionado para receber a coluna automática do Supabase
)