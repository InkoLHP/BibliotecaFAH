package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf12_tela_livro)

        progressBar = findViewById(R.id.progressBarDetalhes)

        val livro = intent.getSerializableExtra("livro") as? Livro

        if (livro != null) {
            mostrarLivro(livro)
        } else {
            Toast.makeText(this, "Livro não encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.buttonLer).setOnClickListener {
            val intent = Intent(this, TelaRF14LeituraActivity::class.java)
            intent.putExtra("livro", livro)
            startActivity(intent)
        }
    }

    private fun mostrarLivro(livro: Livro) {

        findViewById<TextView>(R.id.textTituloLivro).text = livro.titulo

        findViewById<TextView>(R.id.textAutorLivro).text = livro.autor

        findViewById<TextView>(R.id.textSobreLivro).text =
            livro.sinopse ?: "Sinopse não disponível."

        findViewById<ImageView>(R.id.imageLivroDetalhes).load(livro.capaUrl) {
            crossfade(true)
            placeholder(R.drawable.osda)
            error(R.drawable.osda)
        }
    }
}