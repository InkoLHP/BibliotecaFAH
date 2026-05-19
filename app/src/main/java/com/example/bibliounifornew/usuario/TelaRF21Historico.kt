package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF21Historico : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf21_historico)


        val buttonRemoverHistorico = findViewById<Button>(R.id.btnRemoverHIstorico)
        buttonRemoverHistorico?.setOnClickListener {
            // Lógica para remover item do histórico
        }
    }
}