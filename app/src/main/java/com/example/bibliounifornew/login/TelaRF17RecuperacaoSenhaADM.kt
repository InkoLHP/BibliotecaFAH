package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.data.User
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF17RecuperacaoSenhaADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_recuperacao_senha_adm)

        // CAMPOS
        val email = findViewById<EditText>(R.id.editEmailRecuperar)

        // TEXTOS
        val erro = findViewById<TextView>(R.id.textErroEmailRecuperar)
        val voltarLogin = findViewById<TextView>(R.id.textVoltarLogin)

        // BOTÃO
        val botaoEnviar = findViewById<MaterialButton>(R.id.buttonEnviarCodigo)

        // ESCONDER ERRO INICIALMENTE
        erro.visibility = View.GONE

        // BOTÃO ENVIAR (UNIFICADO E COM CONEXÃO AO SUPABASE)
        botaoEnviar.setOnClickListener {

            val textoEmail = email.text.toString().trim()

            erro.visibility = View.GONE

            when {
                textoEmail.isEmpty() -> {
                    erro.text = "Digite um e-mail"
                    erro.visibility = View.VISIBLE
                }

                !Patterns.EMAIL_ADDRESS.matcher(textoEmail).matches() -> {
                    erro.text = "Formato de e-mail inválido"
                    erro.visibility = View.VISIBLE
                }

                else -> {
                    // Desativa o botão para evitar múltiplos cliques enquanto consulta o banco
                    botaoEnviar.isEnabled = false

                    lifecycleScope.launch {
                        try {
                            // Consulta se o e-mail existe e pertence a um perfil do tipo "adm"
                            val admExistente = withContext(Dispatchers.IO) {
                                SupabaseConfig.client.postgrest["users"]
                                    .select {
                                        filter {
                                            eq("email", textoEmail)
                                            eq("tipo", "adm")
                                        }
                                    }.decodeSingleOrNull<User>()
                            }

                            if (admExistente != null) {
                                // 1. Gera o código e guarda no CodigoManager
                                val codigoGerado = com.example.bibliounifornew.api.CodigoManager.gerarCodigo()
                                com.example.bibliounifornew.api.CodigoManager.emailRecuperacao = textoEmail

                                // 2. Envia o e-mail
                                com.example.bibliounifornew.utils.EmailSender.enviarEmail(
                                    email = textoEmail,
                                    codigo = codigoGerado,
                                    onSuccess = {
                                        runOnUiThread {
                                            botaoEnviar.isEnabled = true
                                            Toast.makeText(this@TelaRF17RecuperacaoSenhaADM, "Código enviado com sucesso!", Toast.LENGTH_SHORT).show()
                                            
                                            // 3. Abre a tela de validação do código
                                            val intent = Intent(this@TelaRF17RecuperacaoSenhaADM, TelaRF18ValidaçãoCodigoADM::class.java)
                                            intent.putExtra("USER_EMAIL", textoEmail)
                                            startActivity(intent)
                                        }
                                    },
                                    onError = {
                                        runOnUiThread {
                                            botaoEnviar.isEnabled = true
                                            Toast.makeText(this@TelaRF17RecuperacaoSenhaADM, "Erro ao enviar e-mail. Tente novamente.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            } else {
                                // Se o e-mail não existir ou não for um ADM
                                erro.text = "E-mail de administrador não cadastrado"
                                erro.visibility = View.VISIBLE
                                botaoEnviar.isEnabled = true
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@TelaRF17RecuperacaoSenhaADM, "Erro ao conectar ao banco", Toast.LENGTH_SHORT).show()
                            botaoEnviar.isEnabled = true
                        }
                    }
                }
            }
        }

        // VOLTAR LOGIN
        voltarLogin.setOnClickListener {
            finish()
        }

        // UX MELHORADA
        email.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                erro.visibility = View.GONE
            }
        }
    }
}