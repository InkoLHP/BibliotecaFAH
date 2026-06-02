package com.example.bibliounifornew.api

object CodigoManager {

    var codigoGerado: String = ""
    var emailRecuperacao: String = ""
    var timestampCriacao: Long = 0L

    fun gerarCodigo(): String {
        codigoGerado = (100000..999999).random().toString()
        timestampCriacao = System.currentTimeMillis()
        return codigoGerado
    }

    fun estaExpirado(): Boolean {
        val tempoDecorrido = System.currentTimeMillis() - timestampCriacao
        return tempoDecorrido > 120000
    }
}