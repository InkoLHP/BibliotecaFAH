package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF19Solicitacoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes)


        // 1. Localizar os botões pelos IDs do XML
        val btnPdf = findViewById<Button>(R.id.buttonSolicitarPdf)
        val btnBraile = findViewById<Button>(R.id.buttonSolicitarBraille)
        val btnAudio = findViewById<Button>(R.id.buttonSolicitarAudiobook)
        val btnReservar = findViewById<Button>(R.id.buttonReservarLivro)
        val btnSetor = findViewById<Button>(R.id.buttonSetorLocalizado)

        // 2. Configurar apenas o botão PDF para levar aos Termos e Condições
        btnPdf.setOnClickListener {
            val intent = Intent(this, TelaRF19SolicitacoesTermosCondicoes::class.java)
            startActivity(intent)
        }

        btnSetor.setOnClickListener {
            Toast.makeText(
                this,
                "Setor do livro O Alienista: Setor X",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 3. Os outros botões ficam sem ação definida por enquanto
        btnBraile.setOnClickListener { /* Sem ação */ }
        btnAudio.setOnClickListener { /* Sem ação */ }
        btnReservar.setOnClickListener { /* Sem ação */ }
    }
}