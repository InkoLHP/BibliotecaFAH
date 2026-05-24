package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.api.CodigoManager
import com.example.bibliounifornew.utils.EmailSender
import java.util.Locale

class TelaRF18ValidaçãoCodigoADM : AppCompatActivity() {

    // Variável para armazenar o email do administrador recebido da tela anterior
    private var emailADM: String? = null
    private var countDownTimer: CountDownTimer? = null
    private val tempoTotal: Long = 120000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf06_validacao_de_codigo)

        // Captura o email que veio da tela anterior do ADM
        emailADM = intent.getStringExtra("USER_EMAIL")

        val editCodigo = findViewById<EditText>(R.id.editTextCodigo)
        val textErro = findViewById<TextView>(R.id.textErroCodigo)
        val buttonReenviarCod = findViewById<TextView>(R.id.textReenviarCodigo)
        val buttonEnviarCodigo = findViewById<Button>(R.id.buttonEnviarCodigo)
        val timer = findViewById<TextView>(R.id.textTimer)

        // Inicialmente o erro fica invisível
        textErro.visibility = View.GONE

        iniciarTimer(buttonReenviarCod, timer)

        buttonEnviarCodigo.setOnClickListener {
            val codigoDigitado = editCodigo.text.toString().trim()

            // Valida contra o código dinâmico gerado no seu CodigoManager
            if (codigoDigitado.isNotEmpty() && (codigoDigitado == CodigoManager.codigoGerado)) {
                textErro.visibility = View.GONE

                // Para o timer
                countDownTimer?.cancel()

                val intent = Intent(this, TelaRF19RedefinirSenhaADM::class.java)
                // 🔥 Envia o email adiante para a tela de redefinição do ADM
                intent.putExtra("USER_EMAIL", emailADM)
                startActivity(intent)
                finish() 
            } else {
                // Código incorreto
                textErro.visibility = View.VISIBLE
                textErro.text = "Código incorreto. Verifique seu e-mail."
            }
        }

        buttonReenviarCod.setOnClickListener {
            val novoCodigo = CodigoManager.gerarCodigo()
            val email = emailADM ?: CodigoManager.emailRecuperacao

            Toast.makeText(this, "Reenviando código...", Toast.LENGTH_SHORT).show()

            // Dispara o e-mail novamente
            EmailSender.enviarEmail(
                email = email,
                codigo = novoCodigo,
                onSuccess = {
                    runOnUiThread {
                        // Reinicia o timer
                        iniciarTimer(buttonReenviarCod, timer)
                        Toast.makeText(this@TelaRF18ValidaçãoCodigoADM, "Novo código enviado com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = {
                    runOnUiThread {
                        Toast.makeText(this@TelaRF18ValidaçãoCodigoADM, "Falha ao reenviar. Tente novamente.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun iniciarTimer(tvReenviar: TextView, tvTimer: TextView) {
        tvReenviar.isEnabled = false
        tvReenviar.alpha = 0.5f

        countDownTimer?.cancel() 

        countDownTimer = object : CountDownTimer(tempoTotal, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutos = (millisUntilFinished / 1000) / 60
                val segundos = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos)
            }

            override fun onFinish() {
                tvTimer.text = "00:00"
                tvReenviar.isEnabled = true
                tvReenviar.alpha = 1.0f
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() 
    }
}
