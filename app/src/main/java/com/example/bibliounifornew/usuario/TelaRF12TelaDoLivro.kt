package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifor.data.AppDatabase
import com.example.bibliounifornew.R
import com.example.bibliounifornew.usuario.TelaRF19Solicitacoes
import kotlinx.coroutines.launch

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val libroDao by lazy { database.livroDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf12_teladolivro)

        val livroId = intent.getIntExtra("LIVRO_ID", -1)
        if (livroId != -1) {
            carregarDadosDoLivro(livroId)
        }

        findViewById<Button>(R.id.buttonVerMais).setOnClickListener {
            val intent = Intent(this, TelaRF13VerMaisLivro::class.java)
            intent.putExtra("LIVRO_ID", livroId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.buttonSolicitar).setOnClickListener {
            val intent = Intent(this, TelaRF19Solicitacoes::class.java)
            intent.putExtra("LIVRO_ID", livroId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.buttonLer).setOnClickListener {
            val intent = Intent(this, TelaRF14LeituraActivity::class.java)
            intent.putExtra("LIVRO_ID", livroId)
            startActivity(intent)
        }
    }

    private fun carregarDadosDoLivro(id: Int) {
        lifecycleScope.launch {
            val livro = libroDao.buscarLivroPorId(id)
            livro?.let {
                findViewById<TextView>(R.id.textTituloLivro).text = it.title
                findViewById<TextView>(R.id.textAutorLivro).text = it.author
                findViewById<TextView>(R.id.textSobreLivro).text = it.content

                val imgCapa = findViewById<ImageView>(R.id.imageLivroDetalhes)
                if (it.coverResourceId != 0) {
                    imgCapa.setImageResource(it.coverResourceId)
                } else {
                    imgCapa.setImageResource(R.drawable.osda)
                }
            }
        }
    }
}