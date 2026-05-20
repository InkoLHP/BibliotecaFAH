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
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.google.android.material.button.MaterialButton

class TelaRF09Configuracao : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf09_configuracao)

        // MAPEAMENTO DOS BOTÕES PRINCIPAIS
        val btnRedefinir = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)
        val btnApagar = findViewById<MaterialButton>(R.id.buttonApagarConta)

        // Ir para a tela de redefinir senha
        btnRedefinir.setOnClickListener {
            val intent = Intent(this, TelaRF10RedefinirSenha::class.java)
            startActivity(intent)
        }

        // Abrir o pop-up de apagar conta
        btnApagar.setOnClickListener {
            exibirPopupApagarConta()
        }

        // LÓGICA PARA EDITAR USUÁRIO
        val btnEditarUsuario = findViewById<ImageView>(R.id.btnEditarUsuario)
        val textUsuario = findViewById<TextView>(R.id.textUsuario)

        btnEditarUsuario.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Editar Usuário")

            // Criar um EditText dinamicamente para o popup
            val input = EditText(this)
            input.hint = "Digite o novo nome ou email"
            input.setText(textUsuario.text)
            builder.setView(input)

            builder.setPositiveButton("Salvar") { dialog, _ ->
                val novoNome = input.text.toString().trim()
                if (novoNome.isNotEmpty()) {
                    textUsuario.text = novoNome
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }
    }

    // --- FUNÇÃO DO POP-UP DE APAGAR CONTA COM VALIDAÇÃO ---
    private fun exibirPopupApagarConta() {
        // 1. Infla o layout do XML do pop-up
        val dialogView = layoutInflater.inflate(R.layout.popup_apagar_conta, null)

        // 2. Constrói o AlertDialog
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true) // Permite fechar se clicar fora do pop-up
            .create()

        // 3. Mapeia os elementos de DENTRO do pop-up
        val editSenhaPopup = dialogView.findViewById<EditText>(R.id.editSenhaPopup)
        val textErroSenhaPopup = dialogView.findViewById<TextView>(R.id.textErroSenhaPopup)
        val buttonConfirmarApagarConta = dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)

        // 4. Configura a ação de clique do botão confirmar
        buttonConfirmarApagarConta.setOnClickListener {
            val senhaDigitada = editSenhaPopup.text.toString().trim()

            // Valida se a senha digitada é a senha de teste "1234"
            if (senhaDigitada == "1234") {
                textErroSenhaPopup.visibility = View.GONE
                alertDialog.dismiss() // Fecha o pop-up

                // Mostra aviso de sucesso
                Toast.makeText(this, "Conta apagada com sucesso!", Toast.LENGTH_SHORT).show()

                // Redireciona para a tela de Boas-Vindas limpando o histórico de navegação
                val intent = Intent(this, TelaRF01BemVindo::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Exibe o texto de erro caso a senha esteja incorreta
                textErroSenhaPopup.text = "Senha incorreta!"
                textErroSenhaPopup.visibility = View.VISIBLE
            }
        }

        // 5. Exibe o pop-up na tela
        alertDialog.show()
    }
}