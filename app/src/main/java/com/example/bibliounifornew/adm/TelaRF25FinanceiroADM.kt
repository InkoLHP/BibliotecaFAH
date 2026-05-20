package com.example.bibliounifornew.adm

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF25FinanceiroADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mantive o nome do layout exatamente como você enviou
        setContentView(R.layout.telarf25_finaceiro_adm)

        val buttonVerPendentes = findViewById<Button>(R.id.btnVerPendentes)

        // tem que fazer a integração dessa tela com o banco de dados, separar o card de livro aí e fazer o sisteminha de lista dinâminca eu creio

        buttonVerPendentes.setOnClickListener {
            // lógica de quando apertar o botão btnVerPendentes
        }

    }
}