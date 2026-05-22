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
import com.example.bibliounifornew.usuario.UsuarioMainActivity
// 1. IMPORTAÇÃO ATUALIZADA AQUI:
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF03LoginAluno : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf03_loginaluno)

        // CAMPOS
        val email = findViewById<EditText>(R.id.editEmail)
        val senha = findViewById<EditText>(R.id.editSenha)

        // BOTÕES
        val botaoEntrar = findViewById<Button>(R.id.buttonEntrar)
        val MostrarSenha = findViewById<ImageView>(R.id.iconOlhoSenha)

        // TEXTOS
        val erro = findViewById<TextView>(R.id.textErroLogin)
        val criarConta = findViewById<TextView>(R.id.textCriarConta)
        val esqueceuSenha = findViewById<TextView>(R.id.textEsqueceuSenha)

        erro.visibility = View.GONE

        // LOGIN COM CONEXÃO REAL AO SUPABASE
        botaoEntrar.setOnClickListener {

            val textoEmail = email.text.toString().trim()
            val textoSenha = senha.text.toString().trim()

            erro.visibility = View.GONE

            if (textoEmail.isEmpty() || textoSenha.isEmpty()) {
                erro.text = "Preencha todos os campos"
                erro.visibility = View.VISIBLE
            } else {
                // Desativa o botão temporariamente para processar a requisição de rede
                botaoEntrar.isEnabled = false

                lifecycleScope.launch {
                    try {
                        // Faz a busca no banco pelo e-mail, senha correspondente e tipo "usuario"
                        val contaEstudante = withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"]
                                .select {
                                    filter {
                                        eq("email", textoEmail)
                                        eq("senha", textoSenha)
                                        eq("tipo", "usuario")
                                    }
                                }.decodeSingleOrNull<User>()
                        }

                        if (contaEstudante != null) {
                            Toast.makeText(this@TelaRF03LoginAluno, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()

                            // 2. MUDANÇA PRINCIPAL AQUI:
                            // Agora o login manda o aluno para a MainActivityUsuario
                            val intent = Intent(this@TelaRF03LoginAluno, UsuarioMainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Caso não encontre nenhum registro correspondente
                            erro.text = "E-mail ou senha incorretos"
                            erro.visibility = View.VISIBLE
                            botaoEntrar.isEnabled = true
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@TelaRF03LoginAluno, "Erro ao conectar ao servidor", Toast.LENGTH_SHORT).show()
                        botaoEntrar.isEnabled = true
                    }
                }
            }
        }

        // CRIAR CONTA -> TelaRF04
        criarConta.setOnClickListener {
            val intent = Intent(this, TelaRF04CadastroNovoUsuario::class.java)
            startActivity(intent)
        }

        // ESQUECEU SENHA -> TelaRF05
        esqueceuSenha.setOnClickListener {
            val intent = Intent(this, TelaRF05RecuperacaoSenha::class.java)
            // Se o usuário já tiver digitado algo no campo de e-mail, repassamos para facilitar a vida dele
            intent.putExtra("USER_EMAIL", email.text.toString().trim())
            startActivity(intent)
        }

        // UX MELHORADA (remove erro ao focar)
        email.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) erro.visibility = View.GONE
        }

        senha.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) erro.visibility = View.GONE
        }

        var senhaVisivel = false

        // Mostrar / Esconder senha
        MostrarSenha.setOnClickListener {
            if (senhaVisivel) {
                // ESCONDER SENHA
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                MostrarSenha.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false
            } else {
                // MOSTRAR SENHA
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                MostrarSenha.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }
            // Mantém o cursor posicionado no final do texto
            senha.setSelection(senha.text.length)
        }
    }
}