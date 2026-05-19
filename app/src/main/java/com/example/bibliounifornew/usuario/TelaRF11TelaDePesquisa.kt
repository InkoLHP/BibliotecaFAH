package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

class TelaRF11TelaDePesquisa : AppCompatActivity() {

    private lateinit var recyclerLivros: RecyclerView
    private lateinit var editPesquisarLivro: EditText
    private lateinit var buttonProcurar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf11_tela_pesquisa)

        recyclerLivros = findViewById(R.id.recyclerLivros)
        editPesquisarLivro = findViewById(R.id.editPesquisarLivro)
        buttonProcurar = findViewById(R.id.buttonProcurar)

        recyclerLivros.layoutManager = LinearLayoutManager(this)

        // LISTA VAZIA INICIALMENTE
        recyclerLivros.adapter = LivroUsuarioAdapter(emptyList()) {
            abrirTelaLivro()
        }

        buttonProcurar.setOnClickListener {

            val pesquisa = editPesquisarLivro.text.toString()

            // EXEMPLO TEMPORÁRIO
            // depois tu troca pelos dados do banco

            val livrosFake = listOf(
                Livro(
                    "O Senhor dos Anéis",
                    "J. R. R. Tolkien",
                    "8595084750"
                ),
                Livro(
                    "Dom Casmurro",
                    "Machado de Assis",
                    "9786559212466"
                )
            )

            recyclerLivros.adapter = LivroUsuarioAdapter(livrosFake) {
                abrirTelaLivro()
            }
        }
    }

    private fun abrirTelaLivro() {
        val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
        startActivity(intent)
    }
}