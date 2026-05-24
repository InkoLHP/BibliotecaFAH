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
import com.example.bibliounifornew.adm.AdmMainActivity // IMPORTAÇÃO CORRETA DA ACTIVITY MÃE
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.data.User
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF16LoginADM : AppCompatActivity() {

    // Credenciais válidas de Administrador
    private val CredencialADM = arrayOf(
        "30062007",
        "01042007",
        "22112006"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf16_login_adm)

        // CAMPOS
        val email = findViewById<EditText>(R.id.editEmailAdm)
        val senha = findViewById<EditText>(R.id.editSenhaAdm)
        val credential = findViewById<EditText>(R.id.editCredencialAdm)

        // BOTÃO
        val botaoEntrar = findViewById<Button>(R.id.buttonEntrarAdm)
        val bntMostraSenha = findViewById<ImageView>(R.id.iconOlhoSenhaAdm)

        // TEXTOS
        val erro = findViewById<TextView>(R.id.textErroAdm)
        val criarConta = findViewById<TextView>(R.id.textCriarContaAdm)
        val esqueceuSenha = findViewById<TextView>(R.id.textEsqueceuSenhaAdm)

        erro.visibility = View.GONE

        // LOGIN CONECTADO AO SUPABASE
        botaoEntrar.setOnClickListener {
            val textoEmail = email.text.toString().trim()
            val textoSenha = senha.text.toString().trim()
            val textoCredencial = credential.text.toString().trim()

            erro.visibility = View.GONE

            if (textoEmail.isEmpty() || textoSenha.isEmpty() || textoCredencial.isEmpty()) {
                erro.text = "Preencha todos os campos"
                erro.visibility = View.VISIBLE
            } else {
                // Desativa o botão para evitar múltiplos cliques na rede
                botaoEntrar.isEnabled = false

                if (textoCredencial in CredencialADM) {
                    lifecycleScope.launch {
                        try {
                            // Busca no banco por todas as credenciais fornecidas + o tipo "adm"
                            val contaAdm = withContext(Dispatchers.IO) {
                                SupabaseConfig.client.postgrest["users"]
                                    .select {
                                        filter {
                                            eq("email", textoEmail)
                                            eq("senha", textoSenha)
                                            eq("credencial", textoCredencial)
                                            eq("tipo", "adm")
                                        }
                                    }.decodeSingleOrNull<User>()
                            }

                            if (contaAdm != null) {
                                android.util.Log.d("LOGIN_DEBUG", "Login ADM Sucesso. Abrindo AdmMainActivity")

                                // SALVA A SESSÃO DO ADM (Nome, E-mail e Tipo)
                                val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putString("USER_NOME", contaAdm.nome)
                                editor.putString("USER_EMAIL", contaAdm.email)
                                editor.putString("USER_TIPO", contaAdm.tipo)
                                editor.apply()

                                Toast.makeText(
                                    this@TelaRF16LoginADM,
                                    "Login realizado com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // CORREÇÃO: Abre a Activity mãe que gerencia os Fragments
                                val intent = Intent(this@TelaRF16LoginADM, AdmMainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            } else {
                                erro.text = "E-mail, senha ou credencial incorretos"
                                erro.visibility = View.VISIBLE
                                botaoEntrar.isEnabled = true
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@TelaRF16LoginADM,
                                "Erro ao conectar ao servidor",
                                Toast.LENGTH_SHORT
                            ).show()
                            botaoEntrar.isEnabled = true
                        }
                    }
                } else {
                    erro.text = "Essa credencial não está cadastrada!"
                    erro.visibility = View.VISIBLE
                    botaoEntrar.isEnabled = true
                }
            }
        }

        // CRIAR CONTA -> TelaRF20
        criarConta.setOnClickListener {
            val intent = Intent(this, TelaRF20NovaContaADM::class.java)
            startActivity(intent)
        }

        // ESQUECEU SENHA -> TelaRF17
        esqueceuSenha.setOnClickListener {
            val intent = Intent(this, TelaRF17RecuperacaoSenhaADM::class.java)
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

        bntMostraSenha.setOnClickListener {
            if (senhaVisivel) {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                bntMostraSenha.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false
            } else {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bntMostraSenha.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }
            senha.setSelection(senha.text.length)
        }
    }
}