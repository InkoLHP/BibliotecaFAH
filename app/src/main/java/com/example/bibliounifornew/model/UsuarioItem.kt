package com.example.bibliounifornew.model

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioItem(
    val id: Long? = null,
    val nome: String? = null,
    val usuario: String? = null,
    val email: String? = null,
    val tipo: String? = null,
    val foto: String? = null,
    val bio: String? = null
)