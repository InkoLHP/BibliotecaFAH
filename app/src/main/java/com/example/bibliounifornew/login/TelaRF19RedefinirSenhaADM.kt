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

class TelaRF19RedefinirSenhaADM : AppCompatActivity() {

    private var emailAdm: String? = null
    private var senhaAntigaBanco: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_redefinir_senha_adm)

        // Captura o e-mail do Administrador vindo das telas anteriores de validação
        emailAdm = intent.getStringExtra("USER_EMAIL")

        // CAMPOS
        val senhaNova = findViewById<EditText>(R.id.editSenhaNova)
        val confirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)

        // BOTÃO
        val botaoRedefinir = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)

        // ÍCONES
        val olhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaNova)
        val olhoConfirmar = findViewById<ImageView>(R.id.iconOlhoConfirmarSenha)

        // ERROS
        val erroSenha1 = findViewById<TextView>(R.id.textErroSenha1)
        val erroSenha2 = findViewById<TextView>(R.id.textErroSenha2)
        val erroSenhaIgual = findViewById<TextView>(R.id.textErroSenhaIgual)
        val erroSenhaDiferente = findViewById<TextView>(R.id.textErroSenhaDiferente)
        val regrasSenha = findViewById<TextView>(R.id.textRegrasSenha)

        // Esconde todos os alertas e requisitos no início
        ocultarErros(erroSenha1, erroSenha2, erroSenhaIgual, erroSenhaDiferente, regrasSenha)

        // Busca a senha atual no banco para validação futura
        carregarSenhaAntiga()

        // CONTROLE VISIBILIDADE SENHA
        configurarOlhoSenha(olhoSenha, senhaNova)
        configurarOlhoSenha(olhoConfirmar, confirmarSenha)

        // BOTÃO REDEFINIR
        botaoRedefinir.setOnClickListener {
            val textoSenha = senhaNova.text.toString().trim()
            val textoConfirmar = confirmarSenha.text.toString().trim()

            // RESET ERROS AO CLICAR
            ocultarErros(erroSenha1, erroSenha2, erroSenhaIgual, erroSenhaDiferente, regrasSenha)

            var valido = true

            // 1. Verifica campos vazios
            if (textoSenha.isEmpty()) {
                erroSenha1.text = "Campo obrigatório"
                erroSenha1.visibility = View.VISIBLE
                valido = false
            }
            if (textoConfirmar.isEmpty()) {
                erroSenha2.text = "Campo obrigatório"
                erroSenha2.visibility = View.VISIBLE
                valido = false
            }

            if (!valido) return@setOnClickListener

            // 2. Validação de requisitos (Força da Senha)
            val regexForcaSenha = Regex("^(?=.*[A-Z])(?=.*[0-9]).{8,}\$")
            if (!regexForcaSenha.matches(textoSenha)) {
                regrasSenha.text = "A senha deve conter pelo menos 8 caracteres, um número e uma letra maiúscula!"
                regrasSenha.visibility = View.VISIBLE
                valido = false
            }

            // 3. Verifica se é igual a anterior
            if (senhaAntigaBanco != null && textoSenha == senhaAntigaBanco) {
                erroSenhaIgual.visibility = View.VISIBLE
                valido = false
            }

            // 4. Verifica se coincidem
            if (textoSenha != textoConfirmar) {
                erroSenhaDiferente.visibility = View.VISIBLE
                valido = false
            }

            if (valido) {
                if (emailAdm.isNullOrBlank()) {
                    Toast.makeText(this, "Erro: Identificação do administrador não encontrada.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                botaoRedefinir.isEnabled = false

                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].update(
                                { set("senha", textoSenha) }
                            ) {
                                filter { eq("email", emailAdm!!) }
                            }
                        }
                        mostrarPopupSucesso()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@TelaRF19RedefinirSenhaADM, "Erro ao atualizar senha no servidor", Toast.LENGTH_SHORT).show()
                        botaoRedefinir.isEnabled = true
                    }
                }
            }
        }
    }

    private fun carregarSenhaAntiga() {
        if (emailAdm.isNullOrBlank()) return

        lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .select { filter { eq("email", emailAdm!!) } }
                        .decodeSingleOrNull<User>()
                }
                senhaAntigaBanco = user?.senha
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun configurarOlhoSenha(icone: ImageView, campoTexto: EditText) {
        var senhaVisivel = false
        icone.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                campoTexto.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                icone.setImageResource(R.drawable.ic_eye_open)
            } else {
                campoTexto.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                icone.setImageResource(R.drawable.ic_eye_open)
            }
            campoTexto.setSelection(campoTexto.text.length)
        }
    }

    private fun ocultarErros(vararg textViews: TextView) {
        for (tv in textViews) {
            tv.visibility = View.GONE
        }
    }

    private fun mostrarPopupSucesso() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.popup_confirmar_redefinir_senha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val botaoRetornar = dialog.findViewById<Button>(R.id.btnRetornarLogin)
        botaoRetornar.setOnClickListener {
            val intent = Intent(this, TelaRF16LoginADM::class.java)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }
}
