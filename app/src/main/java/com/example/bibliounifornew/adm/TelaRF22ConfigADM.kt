package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF02Intermediaria
import com.google.android.material.button.MaterialButton
import org.w3c.dom.Text

class TelaRF22ConfigADM : AppCompatActivity() {

    private lateinit var olhoADMconfig: ImageView
    private lateinit var editSenhaADMconfig: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf22_config_adm)

        olhoADMconfig = findViewById(R.id.iconOlhoSenhaAtual)
        editSenhaADMconfig = findViewById(R.id.editSenhaAtual)


        // Botões específicos da tela de configuração
        val btnRedefinirSenha = findViewById<MaterialButton>(R.id.btnRedefinirSenha)
        val btnApagarConta = findViewById<MaterialButton>(R.id.btnApagarConta)

        var senhaVisivel = false

        //Mostar senha
        olhoADMconfig.setOnClickListener {
            if (senhaVisivel) {

                // ESCONDER SENHA
                editSenhaADMconfig.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                olhoADMconfig.setImageResource(R.drawable.ic_eye_closed)

                senhaVisivel = false

            } else {

                // MOSTRAR SENHA
                editSenhaADMconfig.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                olhoADMconfig.setImageResource(R.drawable.ic_eye_open)

                senhaVisivel = true
            }

            // Mantém cursor no final
            editSenhaADMconfig.setSelection(editSenhaADMconfig.text.length)
        }

        btnRedefinirSenha?.setOnClickListener {
            val intent = Intent(this, TelaRF23RedefinirADMInterno::class.java)
            startActivity(intent)
        }

        btnApagarConta?.setOnClickListener {

            val dialog = Dialog(this)

            dialog.setContentView(R.layout.popup_apagar_conta)

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // CAMPOS DO POPUP
            val editSenha = dialog.findViewById<EditText>(R.id.editSenhaPopup)
            val textErro = dialog.findViewById<TextView>(R.id.textErroSenhaPopup)

            val btnConfirmar =
                dialog.findViewById<Button>(R.id.buttonConfirmarApagarConta)

            val iconOlho =
                dialog.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

            // SENHA MOCKADA DO ADM
            val senhaAdm = "123456"

            // MOSTRAR / OCULTAR SENHA
            var senhaVisivel = false

            iconOlho.setOnClickListener {

                if (senhaVisivel) {

                    // ESCONDER
                    editSenha.inputType =
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_PASSWORD

                    iconOlho.setImageResource(R.drawable.ic_eye_closed)

                    senhaVisivel = false

                } else {

                    // MOSTRAR
                    editSenha.inputType =
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                    iconOlho.setImageResource(R.drawable.ic_eye_open)

                    senhaVisivel = true
                }

                editSenha.setSelection(editSenha.text.length)
            }

            // CONFIRMAR
            btnConfirmar.setOnClickListener {

                val senhaDigitada = editSenha.text.toString()

                if (senhaDigitada == senhaAdm) {

                    // FECHA POPUP
                    dialog.dismiss()

                    // VOLTA PARA O INÍCIO DO APP
                    val intent = Intent(this, TelaRF02Intermediaria::class.java)

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)

                    finish()

                } else {

                    textErro.visibility = View.VISIBLE
                }
            }

            dialog.show()
        }
    }
}