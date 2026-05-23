package com.example.bibliounifornew

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.login.TelaRF01BemVindo

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redireciona para a Tela de Bem-Vindo logo ao iniciar
        val intent = Intent(this, TelaRF01BemVindo::class.java)
        startActivity(intent)
        finish()
    }
}