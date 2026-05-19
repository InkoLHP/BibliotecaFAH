package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.MainActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF08DashboardUsuario : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf08_dashboardusuario)

        // Botões do Header
        val btnConfig = findViewById<ImageView>(R.id.btnConfig)
        val btnNotificacao = findViewById<ImageView>(R.id.btnNotificacao)
        val profileImage = findViewById<ImageView>(R.id.imagePerfilUsuario)
        val textNomeUsuario = findViewById<TextView>(R.id.textNomeUsuario)

        // Botões de Ações Rápidas (Cards/Buttons no ScrollView)
        //val btnProcurarLivros = findViewById<MaterialButton>(R.id.btnProcurarLivros)
        val btnMinhaLivraria = findViewById<MaterialButton>(R.id.btnMinhaLivraria)
        val btnListaDesejo = findViewById<MaterialButton>(R.id.btnListaDesejos)
        val btnAmigos = findViewById<MaterialButton>(R.id.btnAmigos)
        val btnHistorico = findViewById<MaterialButton>(R.id.btnHistorico)
        val btnStatusAluguel = findViewById<MaterialButton>(R.id.btnStatusAluguel)
        val btnSair = findViewById<MaterialButton>(R.id.btnSairConta)

        // Navegação via Engrenagem -> Configuração (RF10)
        btnConfig.setOnClickListener {
            startActivity(Intent(this, TelaRF09Configuracao::class.java))
        }

        btnNotificacao.setOnClickListener {
            startActivity(Intent(this, TelaRF20Notificacoes::class.java))
        }

        // Ações Rápidas
        //btnProcurarLivros.setOnClickListener {
        //    startActivity(Intent(this, TelaRF12TelaDePesquisa::class.java))
        //}

        btnMinhaLivraria.setOnClickListener {
            startActivity(Intent(this, TelaRF15MinhaLivrariaActivity::class.java))
        }

        btnListaDesejo.setOnClickListener {
            startActivity(Intent(this, TelaRF16ListaDesejosActivity::class.java))
        }

        btnAmigos.setOnClickListener {
            startActivity(Intent(this, TelaRF17Amigos::class.java))
        }

        btnHistorico.setOnClickListener {
            startActivity(Intent(this, TelaRF21Historico::class.java))
        }

        btnStatusAluguel.setOnClickListener {
            startActivity(Intent(this, TelaRF19Solicitacoes::class.java))
        }

        btnSair.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }
}