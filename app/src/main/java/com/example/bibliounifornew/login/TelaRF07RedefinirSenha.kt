package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF07RedefinirSenha : AppCompatActivity() {

    private var emailUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf07_redefinicao_de_senha)

        // Recupera o e-mail que deve ter sido passado pelas telas anteriores
        emailUsuario = intent.getStringExtra("USER_EMAIL")

        val editSenhaNova = findViewById<EditText>(R.id.editSenhaNova)
        val editConfirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)
        val btnConfirmar = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)

        val erroSenha1 = findViewById<TextView>(R.id.textErroSenha1)
        val erroSenha2 = findViewById<TextView>(R.id.textErroSenha2)

        val textErroDiferente = findViewById<TextView>(R.id.textErroSenhaDiferente)
        val textErroIgual = findViewById<TextView>(R.id.textErroSenhaIgual)
        val textErroRequisitos = findViewById<TextView>(R.id.textRegrasSenha)

        val bntOlhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaNova)
        val bntOlhoConfirmarSenha = findViewById<ImageView>(R.id.iconOlhoConfirmarSenha)

        // Inicializa erros como invisíveis
        textErroDiferente.visibility = View.GONE
        textErroIgual.visibility = View.GONE
        textErroRequisitos.visibility = View.GONE
        erroSenha2.visibility = View.GONE
        erroSenha1.visibility = View.GONE

        btnConfirmar.setOnClickListener {
            val senhanova = editSenhaNova.text.toString()
            val confirmarsenha = editConfirmarSenha.text.toString()

            // Reseta alertas visuais de erro de campos vazios
            erroSenha1.visibility = View.GONE
            erroSenha2.visibility = View.GONE
            textErroDiferente.visibility = View.GONE

            val senhaValida = validarSenha(senhanova)
            val senhasIguais = senhanova == confirmarsenha

            if (editSenhaNova.text.toString().isEmpty() || editConfirmarSenha.text.toString().isEmpty()) {
                erroSenha2.visibility = View.VISIBLE
                erroSenha1.visibility = View.VISIBLE
            } else if (senhaValida && senhasIguais) {

                // Se o e-mail não foi repassado corretamente pelas intents, usamos um aviso de segurança
                if (emailUsuario.isNullOrBlank()) {
                    Toast.makeText(this, "Erro: Identificação do usuário não encontrada.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Desativa o botão para evitar cliques múltiplos
                btnConfirmar.isEnabled = false

                lifecycleScope.launch {
                    try {
                        // Atualiza a senha na tabela "users" onde o email for igual ao guardado
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].update(
                                {
                                    set("senha", senhanova)
                                }
                            ) {
                                filter {
                                    eq("email", emailUsuario!!)
                                }
                            }
                        }

                        // Abre o pop-up de sucesso se a operação no banco funcionar
                        mostrarPopupSucesso()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@TelaRF07RedefinirSenha, "Erro ao atualizar a senha no banco", Toast.LENGTH_SHORT).show()
                        btnConfirmar.isEnabled = true
                    }
                }

            } else {
                if (!senhasIguais) {
                    textErroDiferente.visibility = View.VISIBLE
                    textErroDiferente.text = "As senhas não coincidem"
                }

                if (!senhaValida) {
                    textErroRequisitos.visibility = View.VISIBLE
                    textErroRequisitos.setTextColor(
                        ContextCompat.getColor(this, android.R.color.holo_red_dark)
                    )
                } else {
                    textErroRequisitos.visibility = View.GONE
                }
            }
        }

        var senhaVisivel = false
        var confirmarSenhaVisivel = false

        // Mostrar/Esconder Senha Nova
        bntOlhoSenha.setOnClickListener {
            if (senhaVisivel) {
                editSenhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                bntOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false
            } else {
                editSenhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bntOlhoSenha.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }
            editSenhaNova.setSelection(editSenhaNova.text.length)
        }

        // Mostrar/Esconder Confirmar Senha
        bntOlhoConfirmarSenha.setOnClickListener {
            if (confirmarSenhaVisivel) {
                editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                bntOlhoConfirmarSenha.setImageResource(R.drawable.ic_eye_closed)
                confirmarSenhaVisivel = false
            } else {
                editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bntOlhoConfirmarSenha.setImageResource(R.drawable.ic_eye_open)
                confirmarSenhaVisivel = true
            }
            editConfirmarSenha.setSelection(editConfirmarSenha.text.length)
        }
    }

    private fun validarSenha(senha: String): Boolean {
        val temOitoDigitos = senha.length >= 8
        val temMaiuscula = senha.any { it.isUpperCase() }
        val temNumero = senha.any { it.isDigit() }
        return temOitoDigitos && temMaiuscula && temNumero
    }

    private fun mostrarPopupSucesso() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.popup_confirmar_redefinir_senha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val botaoRetornar = dialog.findViewById<Button>(R.id.btnRetornarLogin)
        botaoRetornar.setOnClickListener {
            val intent = Intent(this, TelaRF03LoginAluno::class.java)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }
}