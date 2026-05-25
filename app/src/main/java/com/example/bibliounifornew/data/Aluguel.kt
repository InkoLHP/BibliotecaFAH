package com.example.bibliounifornew.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Aluguel(
    val id: Long = 0,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("email_usuario")
    val emailUsuario: String? = null,

    @SerialName("titulo_livro")
    val tituloLivro: String? = null,

    @SerialName("capa_url")
    val capaUrl: String? = null,

    @SerialName("data_vencimento")
    val dataVencimento: String? = null,

    val devolvido: Boolean? = false,

    @SerialName("autor_livro")
    val autorLivro: String? = null,

    @SerialName("dias_restantes")
    val diasRestantes: Long? = null
)