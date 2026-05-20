package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Livro

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

        // LISTA VAZIA INICIALMENTE - Agora recebe o objeto livro no lambda
        recyclerLivros.adapter = LivroUsuarioAdapter(emptyList()) { livroClicado ->
            abrirTelaLivro(livroClicado)
        }

        buttonProcurar.setOnClickListener {
            val pesquisa = editPesquisarLivro.text.toString()

            // Lista mockada corrigida usando o construtor do novo Livro.kt
            val livrosFake = listOf(
                Livro(
                    id = "1",
                    titulo = "O Senhor dos Anéis",
                    autor = "J. R. R. Tolkien",
                    isbn = "8595084750",
                    sinopse = "A jornada de Frodo para destruir o Um Anel.",
                    capaResourceId = 0
                ),
                Livro(
                    id = "2",
                    titulo = "Dom Casmurro",
                    autor = "Machado de Assis",
                    isbn = "9786559212466",
                    sinopse = "A clássica história de Bentinho, Capitu e a dúvida do ciúme.",
                    capaResourceId = 0
                )
            )

            // Atualiza o adapter passando a função que envia o ID do livro clicado
            recyclerLivros.adapter = LivroUsuarioAdapter(livrosFake) { livroClicado ->
                abrirTelaLivro(livroClicado)
            }
        }
    }

    // Agora recebe o Livro inteiro e passa o ID dele para a tela de detalhes
    private fun abrirTelaLivro(livro: Livro) {
        val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
        intent.putExtra("LIVRO_ID", livro.id)
        startActivity(intent)
    }
}