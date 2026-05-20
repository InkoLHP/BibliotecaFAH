package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.MainActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF03LoginAluno
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

        // Botões de Ações Rápidas
        val btnPesquisa = findViewById<MaterialButton>(R.id.btnPesquisa)
        val btnHistorico = findViewById<MaterialButton>(R.id.btnHistorico)
        val btnStatusAluguel = findViewById<MaterialButton>(R.id.btnStatusAluguel)
        val btnSair = findViewById<MaterialButton>(R.id.btnSairConta)
        val btnSalvarAlteracoes = findViewById<MaterialButton>(R.id.btnSalvarAlteracoes)

        // Navegações com verificação de segurança (?.) para evitar telas brancas
        btnConfig?.setOnClickListener {
            startActivity(Intent(this, TelaRF09Configuracao::class.java))
        }

        btnNotificacao?.setOnClickListener {
            startActivity(Intent(this, TelaRF14Notificacoes::class.java))
        }

        btnPesquisa?.setOnClickListener {
            startActivity(Intent(this, TelaRF11TelaDePesquisa::class.java))
        }

        btnStatusAluguel?.setOnClickListener {
            startActivity(Intent(this, TelaRF13StatusAluguel::class.java))
        }

        btnHistorico?.setOnClickListener {
            startActivity(Intent(this, TelaRF15Historico::class.java))
        }

        // Pop-up do botão sair
        btnSair?.setOnClickListener {
            exibirPopupSair()
        }

        // Toast de salvar alterações
        btnSalvarAlteracoes?.setOnClickListener {
            Toast.makeText(
                this,
                "Alterações salvas com sucesso!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun exibirPopupSair() {
        val dialogView = layoutInflater.inflate(R.layout.popup_sair_conta, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnConfirmarSair = dialogView.findViewById<MaterialButton>(R.id.btnConfirmarSair)

        btnConfirmarSair?.setOnClickListener {
            alertDialog.dismiss()
            val intent = Intent(this, TelaRF03LoginAluno::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        alertDialog.show()
    }
}