package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var id: Int? = null,
    var nome: String,
    var email: String,
    var usuario: String,
    var senha: String,
    var bio: String? = null,
    var foto: String? = null,
    var tipo: String? = null,
    var credencial: String? = null // Adicionado para corrigir o erro do cadastro
)