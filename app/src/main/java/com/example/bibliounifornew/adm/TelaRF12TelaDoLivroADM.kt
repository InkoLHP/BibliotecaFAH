package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.ComentariosAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Avaliacao
import com.example.bibliounifornew.model.Livro
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.random.Random

// 🌟 CORRIGIDO: Agora usa o layout exclusivo do Administrador (telarf12_tela_livro_adm)
class TelaRF12TelaDoLivroADM : Fragment(R.layout.telarf12_tela_livro_adm) {

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerComentarios: RecyclerView
    private lateinit var adapterComentarios: ComentariosAdapter
    private lateinit var textSemComentarios: TextView
    private lateinit var textMediaAvaliacoes: TextView
    private lateinit var buttonEditarInformacoes: MaterialButton

    private var livroSelecionado: Livro? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar os componentes da tela (IDs batendo com telarf12_tela_livro_adm.xml)
        progressBar = view.findViewById(R.id.progressBarDetalhes)
        recyclerComentarios = view.findViewById(R.id.recyclerComentarios)
        textSemComentarios = view.findViewById(R.id.textSemComentarios)
        textMediaAvaliacoes = view.findViewById(R.id.textMediaAvaliacoes)
        buttonEditarInformacoes = view.findViewById(R.id.buttonEditarInformacoes)

        // Configurar o RecyclerView de comentários
        adapterComentarios = ComentariosAdapter(emptyList())
        recyclerComentarios.layoutManager = LinearLayoutManager(requireContext())
        recyclerComentarios.adapter = adapterComentarios

        // 2. Recuperar o objeto Livro de forma segura conforme a versão do Android
        livroSelecionado = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("livro", Livro::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("livro") as? Livro
        }

        // 3. Verificar e carregar os dados
        if (livroSelecionado != null) {
            val livro = livroSelecionado!!
            mostrarLivro(view, livro)
            configurarEstoqueFalso(view)
            carregarComentarios(livro.id)

            buttonEditarInformacoes.setOnClickListener {
                abrirTelaEditarMidia(livro)
            }
        } else {
            Toast.makeText(requireContext(), "Erro ao carregar dados do livro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarComentarios(livroId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val listaAvaliacoes = withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .postgrest["avaliacoes"]
                        .select {
                            filter {
                                eq("livro_id", livroId)
                            }
                        }
                        .decodeList<Avaliacao>()
                }

                if (listaAvaliacoes.isEmpty()) {
                    textSemComentarios.visibility = View.VISIBLE
                    recyclerComentarios.visibility = View.GONE
                    textMediaAvaliacoes.text = "Nenhuma avaliação ainda."
                } else {
                    textSemComentarios.visibility = View.GONE
                    recyclerComentarios.visibility = View.VISIBLE

                    val media = listaAvaliacoes.map { it.nota.toInt() }.average()
                    textMediaAvaliacoes.text = String.format(Locale.getDefault(), "Média: %.1f / 5", media)

                    adapterComentarios = ComentariosAdapter(listaAvaliacoes)
                    recyclerComentarios.adapter = adapterComentarios
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar comentários.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarLivro(view: View, livro: Livro) {
        view.findViewById<TextView>(R.id.textTituloLivro).text = livro.titulo
        view.findViewById<TextView>(R.id.textAutorLivro).text = livro.autor
        view.findViewById<TextView>(R.id.textSobreLivro).text = livro.sinopse ?: "Sinopse não disponível."

        view.findViewById<ImageView>(R.id.imageLivroDetalhes).load(livro.capaUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        view.findViewById<TextView>(R.id.textGeneroLivro).text = livro.categoria ?: "N/I"
        view.findViewById<TextView>(R.id.textDataLivro).text = livro.data_publicacao ?: "N/I"
        view.findViewById<TextView>(R.id.textIsbnLivro).text = livro.isbn

        view.findViewById<TextView>(R.id.textIdiomaLivro).text = "N/I"
        view.findViewById<TextView>(R.id.textEditoraLivro).text = "N/I"
        view.findViewById<TextView>(R.id.textDimensaoLivro).text = "N/I"
        view.findViewById<TextView>(R.id.textPaginasLivro).text = "N/I"

        val eDigital = (livro.formato ?: "").contains("pdf", ignoreCase = true) ||
                (livro.formato ?: "").contains("epub", ignoreCase = true)
        view.findViewById<TextView>(R.id.textPdfDisponivel).text = if (eDigital) "Sim" else "Não"
    }

    private fun configurarEstoqueFalso(view: View) {
        val textDisponibilidade = view.findViewById<TextView>(R.id.textDisponibilidade)
        val textQuantidadeEstoque = view.findViewById<TextView>(R.id.textQuantidadeEstoque)

        val isDisponivel = Random.nextDouble() < 0.75
        val qtd = if (isDisponivel) Random.nextInt(1, 6) else 0

        if (isDisponivel && qtd > 0) {
            textDisponibilidade.text = "Sim"
            textQuantidadeEstoque.text = qtd.toString()
        } else {
            textDisponibilidade.text = "Não Disponível"
            textQuantidadeEstoque.text = "0"
        }
    }

    private fun abrirTelaEditarMidia(livro: Livro) {
        val fragmentEditar = TelaRF37EditarMidia()
        val bundle = Bundle()
        bundle.putString("LIVRO_ID", livro.id)
        fragmentEditar.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragmentEditar)
            .addToBackStack(null)
            .commit()
    }
}
