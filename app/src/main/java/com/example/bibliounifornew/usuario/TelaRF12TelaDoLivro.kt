package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Livro
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private var livroId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf12_tela_livro)

        progressBar = findViewById(R.id.progressBarDetalhes) // Opcional: Adicione um ProgressBar no XML

        // Recupera o ID enviado via Intent pela tela de pesquisa
        livroId = intent.getStringExtra("LIVRO_ID")

        if (!livroId.isNullOrBlank()) {
            carregarDadosDoLivro(livroId!!)
        } else {
            Toast.makeText(this, "Erro ao abrir detalhes: ID ausente.", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.buttonLer).setOnClickListener {
            val intent = Intent(this, TelaRF14LeituraActivity::class.java)
            intent.putExtra("LIVRO_ID", livroId)
            startActivity(intent)
        }
    }

    private fun carregarDadosDoLivro(id: String) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Busca no Supabase o livro com o ID correspondente
                val livro = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["books"]
                        .select {
                            filter {
                                eq("id", id)
                            }
                        }.decodeSingleOrNull<Livro>()
                }

                livro?.let {
                    findViewById<TextView>(R.id.textTituloLivro).text = it.titulo
                    findViewById<TextView>(R.id.textAutorLivro).text = it.autor

                    // Alimenta o bloco 'Sobre o Livro' com a sinopse vinda do banco
                    findViewById<TextView>(R.id.textSobreLivro).text = it.sinopse ?: "Sinopse não disponível."

                    val imgCapa = findViewById<ImageView>(R.id.imageLivroDetalhes)
                    if (it.capaResourceId != 0) {
                        imgCapa.setImageResource(it.capaResourceId)
                    } else {
                        imgCapa.setImageResource(R.drawable.osda) // Imagem padrão de fallback
                    }
                } ?: run {
                    Toast.makeText(this@TelaRF12TelaDoLivro, "O livro solicitado não existe no banco.", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@TelaRF12TelaDoLivro, "Erro ao carregar dados do servidor", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}