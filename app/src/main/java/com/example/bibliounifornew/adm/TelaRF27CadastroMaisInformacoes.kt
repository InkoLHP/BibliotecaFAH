package com.example.bibliounifornew.adm

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bibliounifornew.R

class TelaRF27CadastroMaisInformacoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf27_cadastro_mais_informacoes)

        val editPaginas = findViewById<EditText>(R.id.etPaginas)
        val editCategoria = findViewById<EditText>(R.id.etCategoria)
        val editEditora = findViewById<EditText>(R.id.etEditora)
        val editCapa = findViewById<EditText>(R.id.etCapa)
        val editSinopse = findViewById<EditText>(R.id.etSinopse)

        // ainda n sei se vai ter a tela versões, então sem botão aqui por enquanto

    }
}