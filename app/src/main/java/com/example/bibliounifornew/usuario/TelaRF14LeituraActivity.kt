package com.example.bibliounifornew.usuario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class TelaRF14LeituraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val livroId = intent.getIntExtra("LIVRO_ID", 1)


    }
}