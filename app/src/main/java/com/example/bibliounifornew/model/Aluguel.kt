package com.example.bibliounifornew.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Aluguel(
    @SerialName("id")
    val id: Long? = null,
    @SerialName("email_usuario")
    val email_usuario: String? = "",
    @SerialName("titulo_livro")
    val titulo_livro: String? = "",
    @SerialName("autor_livro")
    val autor_livro: String? = "",
    @SerialName("capa_url")
    val capa_url: String? = null,
    @SerialName("data_vencimento")
    val data_vencimento: String? = "",
    @SerialName("dias_restantes")
    val dias_restantes: Long? = 0L,
    @SerialName("devolvido")
    val devolvido: Boolean? = false,
    @SerialName("oculto_historico")
    val oculto_historico: Boolean? = false,
    @SerialName("tipo")
    val tipo: String? = "ALUGUEL", // Importante ser opcional!
    @SerialName("data_retirada")
    val data_retirada: String? = ""
) : java.io.Serializable