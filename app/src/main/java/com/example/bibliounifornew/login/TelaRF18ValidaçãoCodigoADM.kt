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

    private var emailADM: String? = null
    private var countDownTimer: CountDownTimer? = null
    private val tempoTotal: Long = 120000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf06_validacao_de_codigo)

        emailADM = intent.getStringExtra("USER_EMAIL")

        val editCodigo = findViewById<EditText>(R.id.editTextCodigo)
        val textErro = findViewById<TextView>(R.id.textErroCodigo)
        val buttonReenviarCod = findViewById<TextView>(R.id.textReenviarCodigo)
        val buttonEnviarCodigo = findViewById<Button>(R.id.buttonEnviarCodigo)
        val timer = findViewById<TextView>(R.id.textTimer)

        textErro.visibility = View.GONE

        // Passamos o botão de reenvio para ser controlado pelo cronômetro
        iniciarTimer(buttonReenviarCod, timer)

        buttonEnviarCodigo.setOnClickListener {
            val codigoDigitado = editCodigo.text.toString().trim()

            // 1. Checa se o código bate com o gerado
            if (codigoDigitado.isNotEmpty() && (codigoDigitado == CodigoManager.codigoGerado)) {

                if (CodigoManager.estaExpirado()) {
                    textErro.visibility = View.VISIBLE
                    textErro.text = "Este código expirou!"
                    Toast.makeText(this, "Código expirado!", Toast.LENGTH_SHORT).show()
                } else {
                    textErro.visibility = View.GONE
                    countDownTimer?.cancel()

                    val intent = Intent(this, TelaRF19RedefinirSenhaADM::class.java)
                    intent.putExtra("USER_EMAIL", emailADM)
                    startActivity(intent)
                    finish()
                }
            } else {
                // Código digitado errado
                textErro.visibility = View.VISIBLE
                textErro.text = "Código incorreto. Verifique seu e-mail."
            }
        }

        // Deixamos a ação do clique caso ele queira reenviar de forma consciente dentro dos 2 minutos
        buttonReenviarCod.setOnClickListener {
            val novoCodigo = CodigoManager.gerarCodigo()
            val email = emailADM ?: CodigoManager.emailRecuperacao


            EmailSender.enviarEmail(
                email = email,
                codigo = novoCodigo,
                onSuccess = {
                    runOnUiThread {
                        buttonEnviarCodigo.isEnabled = true
                        buttonEnviarCodigo.alpha = 1.0f
                        textErro.visibility = View.GONE

                        iniciarTimer(buttonReenviarCod, timer)
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
        // Encontra o botão de enviar e o texto de erro para manipulá-los quando o tempo acabar
        val buttonEnviarCodigo = findViewById<Button>(R.id.buttonEnviarCodigo)
        val textErro = findViewById<TextView>(R.id.textErroCodigo)

        // Bloqueia o botão de reenvio no início do cronômetro para evitar spam
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

                textErro.visibility = View.VISIBLE
                textErro.text = "O código expirou!"

                buttonEnviarCodigo.isEnabled = false
                buttonEnviarCodigo.alpha = 0.5f

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