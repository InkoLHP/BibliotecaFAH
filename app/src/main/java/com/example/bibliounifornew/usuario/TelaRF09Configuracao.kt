package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.example.bibliounifornew.model.User
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF09Configuracao : AppCompatActivity() {

    private var emailUsuarioLogado: String? = null
    private lateinit var textUsuario: TextView
    private var objetoUsuarioAtual: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf09_configuracao)

        // Captura o e-mail do usuário logado enviado pela Dashboard
        emailUsuarioLogado = intent.getStringExtra("USER_EMAIL")

        // MAPEAMENTO DOS ELEMENTOS DA TELA
        val btnRedefinir = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)
        val btnApagar = findViewById<MaterialButton>(R.id.buttonApagarConta)
        val btnEditarUsuario = findViewById<ImageView>(R.id.btnEditarUsuario)
        textUsuario = findViewById(R.id.textUsuario)

        // Busca as informações atuais do usuário no Supabase para exibir na tela
        carregarDadosUsuario()

        // Ir para a tela de redefinir senha (repassando o e-mail junto)
        btnRedefinir.setOnClickListener {
            val intent = Intent(this, TelaRF10RedefinirSenha::class.java)
            intent.putExtra("USER_EMAIL", emailUsuarioLogado)
            startActivity(intent)
        }

        // Abrir o pop-up de apagar conta
        btnApagar.setOnClickListener {
            if (objetoUsuarioAtual != null) {
                exibirPopupApagarConta()
            } else {
                Toast.makeText(this, "Aguardando carregamento de dados do servidor...", Toast.LENGTH_SHORT).show()
            }
        }

        // LÓGICA PARA EDITAR USUÁRIO NO BANCO
        btnEditarUsuario.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Editar Usuário")

            val input = EditText(this)
            input.hint = "Digite o novo nome de usuário"
            input.setText(textUsuario.text)
            builder.setView(input)

            builder.setPositiveButton("Salvar") { dialog, _ ->
                val novoNomeUsuario = input.text.toString().trim()
                if (novoNomeUsuario.isNotEmpty() && emailUsuarioLogado != null) {

                    lifecycleScope.launch {
                        try {
                            // Atualiza a coluna "usuario" correspondente ao e-mail no Supabase
                            withContext(Dispatchers.IO) {
                                SupabaseConfig.client.postgrest["users"].update(
                                    {
                                        set("usuario", novoNomeUsuario)
                                    }
                                ) {
                                    filter {
                                        eq("email", emailUsuarioLogado!!)
                                    }
                                }
                            }

                            // Atualiza a UI local e o objeto em memória
                            textUsuario.text = novoNomeUsuario
                            objetoUsuarioAtual = objetoUsuarioAtual?.copy(usuario = novoNomeUsuario)
                            Toast.makeText(this@TelaRF09Configuracao, "Nome de usuário atualizado!", Toast.LENGTH_SHORT).show()

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@TelaRF09Configuracao, "Erro ao salvar no servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }
    }

    // --- BUSCA DADOS DO USUÁRIO NA INICIALIZAÇÃO ---
    private fun carregarDadosUsuario() {
        if (emailUsuarioLogado.isNullOrBlank()) return

        lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .select {
                            filter {
                                eq("email", emailUsuarioLogado!!)
                            }
                        }.decodeSingleOrNull<User>()
                }
                if (user != null) {
                    objetoUsuarioAtual = user
                    textUsuario.text = user.usuario
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- FUNÇÃO DO POP-UP DE APAGAR CONTA COM VALIDAÇÃO REAL ---
    private fun exibirPopupApagarConta() {
        val dialogView = layoutInflater.inflate(R.layout.popup_apagar_conta, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val editSenhaPopup = dialogView.findViewById<EditText>(R.id.editSenhaPopup)
        val textErroSenhaPopup = dialogView.findViewById<TextView>(R.id.textErroSenhaPopup)
        val buttonConfirmarApagarConta = dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)

        buttonConfirmarApagarConta.setOnClickListener {
            val senhaDigitada = editSenhaPopup.text.toString().trim()

            // Valida se a senha digitada bate com a senha do usuário salva no Supabase
            if (senhaDigitada == objetoUsuarioAtual?.senha) {
                textErroSenhaPopup.visibility = View.GONE
                buttonConfirmarApagarConta.isEnabled = false // Evita múltiplos cliques

                lifecycleScope.launch {
                    try {
                        // Deleta permanentemente a linha do usuário logado no Supabase
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].delete {
                                filter {
                                    eq("email", emailUsuarioLogado!!)
                                }
                            }
                        }

                        alertDialog.dismiss()
                        Toast.makeText(this@TelaRF09Configuracao, "Conta excluída com sucesso!", Toast.LENGTH_SHORT).show()

                        // Redireciona para a tela de Boas-Vindas limpando a pilha de telas
                        val intent = Intent(this@TelaRF09Configuracao, TelaRF01BemVindo::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@TelaRF09Configuracao, "Erro ao deletar conta no servidor", Toast.LENGTH_SHORT).show()
                        buttonConfirmarApagarConta.isEnabled = true
                    }
                }
            } else {
                textErroSenhaPopup.text = "Senha incorreta!"
                textErroSenhaPopup.visibility = View.VISIBLE
            }
        }

        alertDialog.show()
    }
}