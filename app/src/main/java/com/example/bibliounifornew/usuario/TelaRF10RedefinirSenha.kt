package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF10RedefinirSenha : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf10_redefinir_senha)

        // CAMPOS
        val editNovaSenha = findViewById<EditText>(R.id.editNovaSenha)
        val editConfirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)
        val btnSalvar = findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        val erroSenha = findViewById<TextView>(R.id.textRegrasSenha)
       // val erroConfirmar = findViewById<TextView>(R.id.textErroConfirmar)

        // 🔥 VALIDAÇÃO + SALVAR
        btnSalvar.setOnClickListener {

            val senha = editNovaSenha.text.toString()
            val confirmar = editConfirmarSenha.text.toString()

            erroSenha.visibility = View.GONE
            //erroConfirmar.visibility = View.GONE

            var valido = true

            if (senha.length < 8) {
                erroSenha.visibility = View.VISIBLE
                erroSenha.text = "Mínimo 8 caracteres"
                valido = false
            }

            if (senha != confirmar) {
                //erroConfirmar.visibility = View.VISIBLE
               // erroConfirmar.text = "Senhas diferentes"
                valido = false
            }

            if (valido) {
                Toast.makeText(this, "Senha alterada!", Toast.LENGTH_SHORT).show()

                // VOLTA PRA RF10
                val intent = Intent(this, TelaRF09Configuracao::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}