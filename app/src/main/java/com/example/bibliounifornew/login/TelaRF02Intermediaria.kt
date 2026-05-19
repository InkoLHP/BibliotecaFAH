package com.example.bibliounifornew.login

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF02Intermediaria : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf02_intermediaria)

        val botaoEstudante = findViewById<MaterialButton>(R.id.btnEstudante)
        val botaoAdmin = findViewById<MaterialButton>(R.id.btnAdmin)

        botaoEstudante.setOnClickListener {
            val intent = Intent(this, TelaRF03LoginAluno::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(intent, options.toBundle())
        }

        botaoAdmin.setOnClickListener {
            val intent = Intent(this, TelaRF16LoginADM::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(intent, options.toBundle())
        }
    }
}