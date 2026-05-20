package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
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
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF04CadastroNovoUsuario : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etUsuario: EditText
    private lateinit var etEmail: EditText
    private lateinit var etSenha: EditText
    private lateinit var etConfirmaSenha: EditText
    private lateinit var tvErroEmail: TextView
    private lateinit var tvErroSenha: TextView
    private lateinit var btnCriar: Button
    private lateinit var btnEntreAqui: TextView

    private lateinit var bntOlhoSenha: ImageView
    private lateinit var bntOlhoConfirmarSenha: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf04_cadastrar_novo_usuario)

        etNome = findViewById(R.id.editTextNome)
        etUsuario = findViewById(R.id.editTextUsuario)
        etEmail = findViewById(R.id.editTextEmail)
        etSenha = findViewById(R.id.editTextSenha)
        etConfirmaSenha = findViewById(R.id.editTextConfirmaSenha)
        tvErroEmail = findViewById(R.id.tvErroEmail)
        tvErroSenha = findViewById(R.id.tvErroSenha)
        btnEntreAqui = findViewById(R.id.textEntreAqui)
        btnCriar = findViewById(R.id.btnCriar)
        bntOlhoSenha = findViewById(R.id.iconOlhoSenha)
        bntOlhoConfirmarSenha = findViewById(R.id.iconOlhoConfirmarSenha)

        btnCriar.setOnClickListener {
            validarECadastrar()
        }

        btnEntreAqui.setOnClickListener {
            irParaLogin()
        }

        tvErroEmail.visibility = View.GONE
        tvErroSenha.visibility = View.GONE

        var senhaVisivel = false
        var confirmarSenhaVisivel = false

        // Mostrar/Esconder Senha
        bntOlhoSenha.setOnClickListener {
            if (senhaVisivel) {
                etSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                bntOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false
            } else {
                etSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bntOlhoSenha.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }
            etSenha.setSelection(etSenha.text.length)
        }

        bntOlhoConfirmarSenha.setOnClickListener {
            if (confirmarSenhaVisivel) {
                etConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                bntOlhoConfirmarSenha.setImageResource(R.drawable.ic_eye_closed)
                confirmarSenhaVisivel = false
            } else {
                etConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bntOlhoConfirmarSenha.setImageResource(R.drawable.ic_eye_open)
                confirmarSenhaVisivel = true
            }
            etConfirmaSenha.setSelection(etConfirmaSenha.text.length)
        }
    }

    private fun validarECadastrar() {
        val nome = etNome.text.toString().trim()
        val usuario = etUsuario.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val senha = etSenha.text.toString()
        val confirmaSenha = etConfirmaSenha.text.toString()

        var valido = true
        tvErroEmail.visibility = View.GONE
        tvErroSenha.visibility = View.GONE

        // Validar campos vazios de nome e usuário
        if (nome.isEmpty() || usuario.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
            valido = false
        }

        // Validar E-mail
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvErroEmail.text = "E-mail inválido"
            tvErroEmail.visibility = View.VISIBLE
            valido = false
        }

        // Validar Senha (8 caracteres, 1 número, 1 maiúscula)
        val senhaRegex = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$".toRegex()
        if (!senhaRegex.matches(senha)) {
            tvErroSenha.text = "A senha deve conter no mínimo 8 caracteres, 1 número e 1 letra maiúscula"
            tvErroSenha.visibility = View.VISIBLE
            valido = false
        } else if (senha != confirmaSenha) {
            tvErroSenha.text = "As senhas não coincidem"
            tvErroSenha.visibility = View.VISIBLE
            valido = false
        }

        if (valido) {
            // Desabilita o botão temporariamente para evitar cliques duplos enquanto envia ao banco
            btnCriar.isEnabled = false

            lifecycleScope.launch {
                try {
                    // 1. Verifica se o e-mail já existe na tabela "users" do Supabase
                    val usuarioExistente = withContext(Dispatchers.IO) {
                        SupabaseConfig.client.postgrest["users"]
                            .select {
                                filter {
                                    eq("email", email)
                                }
                            }.decodeSingleOrNull<User>()
                    }

                    if (usuarioExistente != null) {
                        tvErroEmail.text = "E-mail já cadastrado"
                        tvErroEmail.visibility = View.VISIBLE
                        btnCriar.isEnabled = true
                    } else {
                        // 2. Monta o objeto User para o Supabase
                        val novoUsuario = User(
                            nome = nome,
                            usuario = usuario,
                            email = email,
                            senha = senha,
                            tipo = "usuario", // Define fixo que é usuário comum
                            credencial = null,
                            foto = null
                        )

                        // 3. Insere o novo usuário no banco remoto
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].insert(novoUsuario)
                        }

                        Toast.makeText(this@TelaRF04CadastroNovoUsuario, "Conta criada com sucesso!", Toast.LENGTH_LONG).show()
                        irParaLogin()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@TelaRF04CadastroNovoUsuario, "Erro ao conectar ao banco de dados", Toast.LENGTH_SHORT).show()
                    btnCriar.isEnabled = true
                }
            }
        }
    }

    private fun irParaLogin() {
        val intent = Intent(this, TelaRF03LoginAluno::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}