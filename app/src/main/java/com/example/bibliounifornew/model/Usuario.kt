package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var nome: String,
    var usuario: String,
    var email: String,
    val senha: String,
    val tipo: String,               // Aqui você vai salvar "usuario" ou "adm"
    val credencial: String? = null,  // Pode ser nulo se o tipo for comum
    val foto: String? = null,        // Começa nulo como você definiu
    var bio: String? = null
)