package com.example.bibliounifornew.login

import android.app.Dialog
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
import com.example.bibliounifornew.data.User
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF20NovaContaADM : AppCompatActivity() {

    private lateinit var edtNomeCompleto: EditText
    private lateinit var edtNomeUsuario: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtCredencial: EditText
    private lateinit var edtSenha: EditText
    private lateinit var edtConfirmaSenha: EditText

    //Credenciais
    private val CredencialADM = arrayOf (
        "30062007",
        "01042007",
        "22112006"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf20_nova_conta_adm)

        // CAMPOS
        edtNomeCompleto = findViewById(R.id.editNomeCompletoAdm)
        edtNomeUsuario = findViewById(R.id.editNomeUsuarioAdm)
        edtEmail = findViewById(R.id.editEmailAdmCadastro)
        edtCredencial = findViewById(R.id.editCredencialAdmCadastro)
        edtSenha = findViewById(R.id.editSenhaAdmCadastro)
        edtConfirmaSenha = findViewById(R.id.editConfirmarSenhaAdm)

        // TEXTOS DE ERRO
        val txtErroEmail = findViewById<TextView>(R.id.textErroEmailAdmCadastro)
        val txtErroCredencial = findViewById<TextView>(R.id.textErroCredencialAdm)
        val txtErroSenha = findViewById<TextView>(R.id.textRegrasSenhaAdm)

        // BOTÕES
        val btnCriar = findViewById<Button>(R.id.buttonCriarContaAdm)
        val txtEntreAqui = findViewById<TextView>(R.id.textEntreAquiAdm)

        // ÍCONES
        val iconOlhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaAdmCadastro)
        val iconOlhoConfirma = findViewById<ImageView>(R.id.iconOlhoConfirmarSenhaAdm)

        // ESCONDER ERROS
        txtErroEmail.visibility = View.GONE
        txtErroCredencial.visibility = View.GONE
        txtErroSenha.visibility = View.GONE

        // =========================================
        // MOSTRAR / ESCONDER SENHA
        // =========================================
        var senhaVisivel = false
        iconOlhoSenha.setOnClickListener {
            if (senhaVisivel) {
                edtSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false
            } else {
                edtSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }
            edtSenha.setSelection(edtSenha.text.length)
        }

        // =========================================
        // MOSTRAR / ESCONDER CONFIRMAR SENHA
        // =========================================
        var confirmaVisivel = false
        iconOlhoConfirma.setOnClickListener {
            if (confirmaVisivel) {
                edtConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoConfirma.setImageResource(R.drawable.ic_eye_closed)
                confirmaVisivel = false
            } else {
                edtConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoConfirma.setImageResource(R.drawable.ic_eye_open)
                confirmaVisivel = true
            }
            edtConfirmaSenha.setSelection(edtConfirmaSenha.text.length)
        }

        // =========================================
        // BOTÃO CRIAR CONTA
        // =========================================
        btnCriar.setOnClickListener {

            txtErroEmail.visibility = View.GONE
            txtErroCredencial.visibility = View.GONE
            txtErroSenha.visibility = View.GONE

            val nomeCompleto = edtNomeCompleto.text.toString().trim()
            val nomeUsuario = edtNomeUsuario.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val credencial = edtCredencial.text.toString().trim()
            val senha1 = edtSenha.text.toString()
            val senha2 = edtConfirmaSenha.text.toString()

            var temErro = false

            // VALIDAR CAMPOS VAZIOS BÁSICOS
            if (nomeCompleto.isEmpty() || nomeUsuario.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
                temErro = true
            }

            // VALIDAR EMAIL
            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                txtErroEmail.text = "E-mail inválido"
                txtErroEmail.visibility = View.VISIBLE
                temErro = true
            }

            // VALIDAR CREDENCIAL
            if (credencial.isBlank()) {
                txtErroCredencial.visibility = View.VISIBLE
                temErro = true
            }
            if(credencial !in CredencialADM){
                txtErroCredencial.text = "Credencial invalida!"
                txtErroCredencial.visibility = View.VISIBLE
                temErro = true
            }

            // VALIDAR SENHA
            val temMaiuscula = senha1.any { it.isUpperCase() }
            val temNumero = senha1.any { it.isDigit() }
            val temTamanho = senha1.length >= 8

            if (!temTamanho || !temNumero || !temMaiuscula || senha1 != senha2) {
                txtErroSenha.visibility = View.VISIBLE
                temErro = true
            }

            // SE NÃO HOUVER ERROS DE VALIDAÇÃO LOCAL
            if (!temErro) {
                // Desativa o botão para evitar cliques duplicados
                btnCriar.isEnabled = false

                lifecycleScope.launch {
                    try {
                        // 1. Verifica se já existe um administrador ou usuário cadastrado com esse e-mail
                        val usuarioExistente = withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"]
                                .select {
                                    filter {
                                        eq("email", email)
                                    }
                                }.decodeSingleOrNull<User>()
                        }

                        if (usuarioExistente != null) {
                            txtErroEmail.text = "E-mail já cadastrado"
                            txtErroEmail.visibility = View.VISIBLE
                            btnCriar.isEnabled = true
                        } else {
                            // 2. Cria o objeto contendo o tipo "adm" e a credencial informada
                            val novoAdm = User(
                                nome = nomeCompleto,
                                usuario = nomeUsuario,
                                email = email,
                                senha = senha1,
                                tipo = "adm",
                                credencial = credencial,
                                foto = null
                            )

                            // 3. Salva no banco de dados do Supabase
                            withContext(Dispatchers.IO) {
                                SupabaseConfig.client.postgrest["users"].insert(novoAdm)
                            }

                            // 4. Abre o pop-up de sucesso do seu layout
                            mostrarPopUpSucesso()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@TelaRF20NovaContaADM, "Erro ao salvar no banco de dados", Toast.LENGTH_SHORT).show()
                        btnCriar.isEnabled = true
                    }
                }
            }
        }

        // VOLTAR
        txtEntreAqui.setOnClickListener {
            finish()
        }
    }

    // =========================================
    // POPUP PERSONALIZADO DE SUCESSO
    // =========================================
    private fun mostrarPopUpSucesso() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_sucesso_cadastro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val botaoRetornar = dialog.findViewById<Button>(R.id.btnRetorneLogin)
        botaoRetornar.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }
}