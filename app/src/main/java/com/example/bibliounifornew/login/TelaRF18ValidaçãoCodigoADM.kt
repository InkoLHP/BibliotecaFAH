package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF18ValidaçãoCodigoADM : AppCompatActivity() {

    // Variável para armazenar o email do administrador recebido da tela anterior
    private var emailADM: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf06_validacao_de_codigo)

        // Captura o email que veio da tela anterior do ADM
        emailADM = intent.getStringExtra("USER_EMAIL")

        val editCodigo = findViewById<EditText>(R.id.editTextCodigo)
        val textErro = findViewById<TextView>(R.id.textErroCodigo)
        val buttonReenviarCod = findViewById<TextView>(R.id.textReenviarCodigo)
        val buttonEnviarCodigo = findViewById<Button>(R.id.buttonEnviarCodigo)

        // Inicialmente o erro fica invisível
        textErro.visibility = View.INVISIBLE

        buttonEnviarCodigo.setOnClickListener {
            val codigoDigitado = editCodigo.text.toString()

            // Simulação de código correto (ex: 123456)
            if (codigoDigitado == "123456") {
                textErro.visibility = View.INVISIBLE

                val intent = Intent(this, TelaRF19RedefinirSenhaADM::class.java)
                // 🔥 Envia o email adiante para a tela de redefinição do ADM
                intent.putExtra("USER_EMAIL", emailADM)
                startActivity(intent)
                finish() // Opcional: fecha esta tela de código
            } else {
                // Código incorreto
                textErro.visibility = View.VISIBLE
                textErro.text = "Código incorreto. Verifique seu e-mail."
            }
        }

        buttonReenviarCod.setOnClickListener {
            Toast.makeText(this, "Codigo enviado!", Toast.LENGTH_SHORT).show()
        }
    }
}