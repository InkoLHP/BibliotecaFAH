package com.example.bibliounifornew.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Midia(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("titulo")
    val titulo: String = "",
    @SerialName("autor")
    val autor: String = "",
    @SerialName("isbn")
    val isbn: String = "",
    @SerialName("capa_url")
    val capaUrl: String? = null
)