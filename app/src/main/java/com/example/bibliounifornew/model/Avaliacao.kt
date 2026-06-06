package com.example.bibliounifornew.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Avaliacao(
    @SerialName("id")
    val id: Long? = null,
    @SerialName("livro_id")
    val livro_id: String, 
    @SerialName("nota")
    val nota: Short,
    @SerialName("comentarios")
    val comentarios: String,
    @SerialName("email") // 🌟 Padronizado para bater com a sua sugestão
    val email: String
)