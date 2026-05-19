package com.example.bibliounifornew.adm

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF31Solicitacoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf31_solicitacoes_adm)

        // =========================
        // BOTÕES DA TELA
        // =========================
        val btnFiltro = findViewById<ImageView>(R.id.buttonFiltroMidia)
        val buttonVerSolicitacoes = findViewById<Button>(R.id.buttonVerSolicitacoesUsuario)
        val buttonEnviarAudiobook = findViewById<Button>(R.id.buttonEnviarAudiobook)
        val buttonEnviarPDF = findViewById<Button>(R.id.buttonEnviarPDF)
        val buttonNotificarBraile = findViewById<Button>(R.id.buttonBrailleConcluido)
        val buttonExcluirSolicitacao = findViewById<Button>(R.id.buttonExcluirSolicitacao)

        // =========================
        // ABRIR POPUPS
        // =========================

    }
}