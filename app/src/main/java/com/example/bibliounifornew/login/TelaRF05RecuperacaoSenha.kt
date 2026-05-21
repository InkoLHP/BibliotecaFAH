package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.api.CodigoManager
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.User
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF05RecuperacaoSenha : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf05_recuperacao_senha)

        val etEmail = findViewById<EditText>(R.id.editTextEmailRec)
        val btnEnviar = findViewById<Button>(R.id.buttonEnviarCOD)
        val textErroEmail = findViewById<TextView>(R.id.textErroEmail)
        val voltar = findViewById<TextView>(R.id.buttonVoltarLog)

        textErroEmail.visibility = View.GONE

        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()

            // Validação local do formato do e-mail
            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                textErroEmail.visibility = View.GONE

                // Desativa o botão para evitar múltiplos cliques enquanto consulta o banco
                btnEnviar.isEnabled = false

                lifecycleScope.launch {
                    try {
                        // Consulta no banco se o e-mail inserido existe na tabela "users"
                        val usuarioExistente = withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"]
                                .select {
                                    filter {
                                        eq("email", email)
                                    }
                                }.decodeSingleOrNull<User>()
                        }

                        if (usuarioExistente != null) {
                            // 1. Gera o código e guarda o e-mail no seu CodigoManager Singleton
                            val codigoGerado = CodigoManager.gerarCodigo()
                            CodigoManager.emailRecuperacao = email

                            // 2. Dispara o e-mail usando o seu EmailSender (OkHttp)
                            com.example.bibliounifornew.utils.EmailSender.enviarEmail(
                                email = email,
                                codigo = codigoGerado,
                                onSuccess = {
                                    // Como o OkHttp roda em background, voltamos para a main thread para mudar de tela
                                    runOnUiThread {
                                        btnEnviar.isEnabled = true
                                        Toast.makeText(this@TelaRF05RecuperacaoSenha, "Código enviado com sucesso!", Toast.LENGTH_SHORT).show()

                                        // 3. Abre a tela de validação do código (TelaRF06)
                                        val intent = Intent(this@TelaRF05RecuperacaoSenha, TelaRF06ValidacaoDeCodigo::class.java)
                                        startActivity(intent)
                                    }
                                },
                                onError = {
                                    runOnUiThread {
                                        btnEnviar.isEnabled = true
                                        Toast.makeText(this@TelaRF05RecuperacaoSenha, "Erro ao enviar e-mail. Tente novamente.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@TelaRF05RecuperacaoSenha, "Erro ao conectar ao banco", Toast.LENGTH_SHORT).show()
                        btnEnviar.isEnabled = true
                    }
                }
            } else {
                // Se o formato do e-mail digitado for inválido
                textErroEmail.text = "E-mail inválido"
                textErroEmail.visibility = View.VISIBLE
            }
        }

        voltar.setOnClickListener {
            val intent = Intent(this, TelaRF03LoginAluno::class.java)
            startActivity(intent)
            finish()
        }
    }
}