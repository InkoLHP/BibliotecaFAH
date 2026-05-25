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
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.User
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF07RedefinirSenha : AppCompatActivity() {

    private var emailUsuario: String? = null
    private var senhaAntigaBanco: String? = null

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
        ocultarErros(erroSenha1, erroSenha2, textErroDiferente, textErroIgual, textErroRequisitos)

        // Busca a senha atual no banco para validação
        carregarSenhaAntiga()

        btnConfirmar.setOnClickListener {
            val senhanova = editSenhaNova.text.toString().trim()
            val confirmarsenha = editConfirmarSenha.text.toString().trim()

            // Reseta alertas
            ocultarErros(erroSenha1, erroSenha2, textErroDiferente, textErroIgual, textErroRequisitos)

            var valido = true

            // 1. Verifica campos vazios
            if (senhanova.isEmpty()) {
                erroSenha1.visibility = View.VISIBLE
                valido = false
            }
            if (confirmarsenha.isEmpty()) {
                erroSenha2.visibility = View.VISIBLE
                valido = false
            }

            if (!valido) return@setOnClickListener

            // 2. Validação de requisitos (Força da Senha)
            if (!validarSenha(senhanova)) {
                textErroRequisitos.text = "A senha deve conter pelo menos 8 caracteres, um número e uma letra maiúscula!"
                textErroRequisitos.visibility = View.VISIBLE
                valido = false
            }

            // 3. Verifica se é igual a anterior
            if (senhaAntigaBanco != null && (senhanova == senhaAntigaBanco)) {
                textErroIgual.visibility = View.VISIBLE
                valido = false
            }

            // 4. Verifica se coincidem
            if (senhanova != confirmarsenha) {
                textErroDiferente.visibility = View.VISIBLE
                valido = false
            }

            if (valido) {
                if (emailUsuario.isNullOrBlank()) {
                    Toast.makeText(this, "Erro: Identificação do usuário não encontrada.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                btnConfirmar.isEnabled = false

                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].update(
                                { set("senha", senhanova) },
                            ) {
                                filter { eq("email", emailUsuario!!) }
                            }
                        }

                        mostrarPopupSucesso()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@TelaRF07RedefinirSenha, "Erro ao atualizar a senha no banco", Toast.LENGTH_SHORT).show()
                        btnConfirmar.isEnabled = true
                    }
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

    private fun carregarSenhaAntiga() {
        if (emailUsuario.isNullOrBlank()) return

        lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .select { filter { eq("email", emailUsuario!!) } }
                        .decodeSingleOrNull<User>()
                }
                senhaAntigaBanco = user?.senha
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun ocultarErros(vararg textViews: TextView) {
        for (tv in textViews) {
            tv.visibility = View.GONE
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