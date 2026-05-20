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

        // Esconder todos os alertas e requisitos assim que a tela abre para melhor UX
        erroSenha1.visibility = View.GONE
        erroSenha2.visibility = View.GONE
        erroSenhaIgual.visibility = View.GONE
        erroSenhaDiferente.visibility = View.GONE
        regrasSenha?.visibility = View.GONE

        // CONTROLE VISIBILIDADE SENHA
        var senhaVisivel = false
        var confirmarVisivel = false

        // OLHO SENHA
        olhoSenha.setOnClickListener {
            if (senhaVisivel) {
                senhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false
            } else {
                senhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }
            senhaNova.setSelection(senhaNova.text.length)
        }

        // OLHO CONFIRMAR
        olhoConfirmar.setOnClickListener {
            if (confirmarVisivel) {
                confirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_closed)
                confirmarVisivel = false
            } else {
                confirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_open)
                confirmarVisivel = true
            }
            confirmarSenha.setSelection(confirmarSenha.text.length)
        }

        // BOTÃO REDEFINIR
        botaoRedefinir.setOnClickListener {

            val textoSenha = senhaNova.text.toString().trim()
            val textoConfirmar = confirmarSenha.text.toString().trim()

            // RESET ERROS AO CLICAR
            erroSenha1.visibility = View.GONE
            erroSenha2.visibility = View.GONE
            erroSenhaIgual.visibility = View.GONE
            erroSenhaDiferente.visibility = View.GONE
            regrasSenha?.visibility = View.GONE

            when {
                textoSenha.isEmpty() -> {
                    erroSenha1.text = "Digite uma senha"
                    erroSenha1.visibility = View.VISIBLE
                }

                textoConfirmar.isEmpty() -> {
                    erroSenha2.text = "Confirme a senha"
                    erroSenha2.visibility = View.VISIBLE
                }

                // 🔥 CORREÇÃO: erroSenha1 agora exibe apenas um aviso curto, evitando duplicidade com o bloco de regras embaixo
                textoSenha.length < 8 -> {
                    erroSenha1.text = "Senha fraca"
                    erroSenha1.visibility = View.VISIBLE
                    regrasSenha?.visibility = View.VISIBLE
                }

                !textoSenha.any { it.isDigit() } -> {
                    erroSenha1.text = "Senha fraca"
                    erroSenha1.visibility = View.VISIBLE
                    regrasSenha?.visibility = View.VISIBLE
                }

                !textoSenha.any { it.isUpperCase() } -> {
                    erroSenha1.text = "Senha fraca"
                    erroSenha1.visibility = View.VISIBLE
                    regrasSenha?.visibility = View.VISIBLE
                }

                // Só verifica se são iguais se a senha passar nos requisitos acima
                textoSenha != textoConfirmar -> {
                    erroSenhaDiferente.visibility = View.VISIBLE
                }

                else -> {
                    if (emailAdm.isNullOrBlank()) {
                        Toast.makeText(this, "Erro: Identificação do administrador não encontrada.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    botaoRedefinir.isEnabled = false

                    lifecycleScope.launch {
                        try {
                            val dadosAdm = withContext(Dispatchers.IO) {
                                SupabaseConfig.client.postgrest["users"]
                                    .select {
                                        filter {
                                            eq("email", emailAdm!!)
                                            eq("tipo", "adm")
                                        }
                                    }.decodeSingleOrNull<User>()
                            }

                            if (dadosAdm != null && dadosAdm.senha == textoSenha) {
                                erroSenhaIgual.visibility = View.VISIBLE
                                botaoRedefinir.isEnabled = true
                            } else {
                                withContext(Dispatchers.IO) {
                                    SupabaseConfig.client.postgrest["users"].update(
                                        {
                                            set("senha", textoSenha)
                                        }
                                    ) {
                                        filter {
                                            eq("email", emailAdm!!)
                                        }
                                    }
                                }
                                mostrarPopupSucesso()
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@TelaRF19RedefinirSenhaADM, "Erro ao conectar ao banco", Toast.LENGTH_SHORT).show()
                            botaoRedefinir.isEnabled = true
                        }
                    }
                }
            }
        }

        // UX - Limpa erros ao focar novamente nos inputs
        senhaNova.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                erroSenha1.visibility = View.GONE
                erroSenhaIgual.visibility = View.GONE
                erroSenhaDiferente.visibility = View.GONE
                regrasSenha?.visibility = View.GONE
            }
        }

        confirmarSenha.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                erroSenha2.visibility = View.GONE
                erroSenhaDiferente.visibility = View.GONE
            }
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