package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF10RedefinirSenha : AppCompatActivity() {

    private var emailUsuarioLogado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf10_redefinir_senha)

        // Captura o e-mail do usuário vindo da TelaRF09Configuracao
        emailUsuarioLogado = intent.getStringExtra("USER_EMAIL")

        // CAMPOS
        val editNovaSenha = findViewById<EditText>(R.id.editNovaSenha)
        val editConfirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)
        val btnSalvar = findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        val erroSenha = findViewById<TextView>(R.id.textRegrasSenha)

        // VALIDAÇÃO + SALVAR NO SUPABASE
        btnSalvar.setOnClickListener {

            val senha = editNovaSenha.text.toString()
            val confirmar = editConfirmarSenha.text.toString()

            erroSenha.visibility = View.GONE

            var valido = true

            // Validação de tamanho
            if (senha.length < 8) {
                erroSenha.text = "A senha deve ter no mínimo 8 caracteres"
                erroSenha.visibility = View.VISIBLE
                valido = false
            }

            // Validação de correspondência
            if (senha != confirmar) {
                erroSenha.text = "As senhas não coincidem"
                erroSenha.visibility = View.VISIBLE
                valido = false
            }

            if (valido) {
                // Proteção caso o e-mail não tenha sido repassado
                if (emailUsuarioLogado.isNullOrBlank()) {
                    Toast.makeText(this, "Erro: Identificação do usuário perdida.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Desativa o botão para evitar cliques múltiplos na rede
                btnSalvar.isEnabled = false

                lifecycleScope.launch {
                    try {
                        // Atualiza a senha no Supabase baseando-se no e-mail do usuário logado
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].update(
                                {
                                    set("senha", senha)
                                }
                            ) {
                                filter {
                                    eq("email", emailUsuarioLogado!!)
                                }
                            }
                        }

                        Toast.makeText(this@TelaRF10RedefinirSenha, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()

                        // Volta para a tela de configurações mantendo o histórico e o e-mail ativo nela
                        val intent = Intent(this@TelaRF10RedefinirSenha, TelaRF09Configuracao::class.java)
                        intent.putExtra("USER_EMAIL", emailUsuarioLogado)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Garante que não empilhe telas duplicadas
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@TelaRF10RedefinirSenha, "Erro ao atualizar senha no servidor", Toast.LENGTH_SHORT).show()
                        btnSalvar.isEnabled = true
                    }
                }
            }
        }
    }
}