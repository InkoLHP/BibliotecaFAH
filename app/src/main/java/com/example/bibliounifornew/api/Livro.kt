package com.example.bibliounifornew.model

import java.io.Serializable

data class Livro(
    val id: Int,
    val titulo: String,
    val autor: String,
    val isbn: String,
    val capaUrl: String,
    val sinopse: String?,
    val data_publicacao: String?,
    val categoria: String?,
    val formato: String,
    val disponivel: Boolean,
    val pdfUrl: String?
) : Serializable