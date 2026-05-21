package com.example.bibliounifornew.api

object CodigoManager {

    var codigoGerado: String = ""
    var emailRecuperacao: String = ""

    fun gerarCodigo(): String {
        codigoGerado = (100000..999999).random().toString()
        return codigoGerado
    }
}