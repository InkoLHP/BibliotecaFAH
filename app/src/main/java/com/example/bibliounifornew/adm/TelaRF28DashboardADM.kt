package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF28DashboardADM : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf28_inicial_adm)

        // 1. Botões de Navegação Principal
        val btnTelaCrud = findViewById<MaterialButton>(R.id.buttonCrudAdm)
        val btnVerAlugueis = findViewById<MaterialButton>(R.id.buttonVerAlugueis)
        val btnVerTodosAtrasos = findViewById<MaterialButton>(R.id.buttonVerAtrasos)

        // 2. Novos Botões Adicionados
        val btnVerCadastros = findViewById<MaterialButton>(R.id.buttonVerCadastros)
        val btnVerSolicitacoes = findViewById<MaterialButton>(R.id.buttonVerSolicitacoes)

        // --- Listeners Principais ---
        btnTelaCrud?.setOnClickListener {
            startActivity(Intent(this@TelaRF28DashboardADM, TelaRF28CrudADM::class.java))
        }

        btnVerAlugueis?.setOnClickListener {
            startActivity(Intent(this@TelaRF28DashboardADM, TelaRF36ListaAlugueisADM::class.java))
        }

        btnVerTodosAtrasos?.setOnClickListener {
            startActivity(Intent(this@TelaRF28DashboardADM, TelaRF34FinanceiroADM::class.java))
        }

        // --- Novos Listeners ---
        btnVerCadastros?.setOnClickListener {
            startActivity(Intent(this@TelaRF28DashboardADM, TelaRF35ConfirmarCadastroADM::class.java))
        }

    }

}