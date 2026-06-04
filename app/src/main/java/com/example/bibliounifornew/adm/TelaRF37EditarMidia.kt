package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Midia // 🌟 Importando o seu modelo correto
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF37EditarMidia : Fragment(R.layout.telarf37_editar_midia) {

    private var livroTitulo: String? = null
    private var livroAutorExterna: String? = null
    private var livroCapaExterna: String? = null

    // Componentes visuais
    private lateinit var textTitulo: TextView
    private lateinit var textAutor: TextView
    private lateinit var textSobre: TextView
    private lateinit var textEditora: TextView
    private lateinit var textIsbn13: TextView
    private lateinit var textPaginas: TextView
    private lateinit var imageLivroDetalhes: ImageView
    private lateinit var buttonApagarMidia: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Resgata os dados enviados pela tela de Aluguéis
        livroTitulo = arguments?.getString("LIVRO_TITULO")
        livroAutorExterna = arguments?.getString("LIVRO_AUTOR")
        livroCapaExterna = arguments?.getString("LIVRO_CAPA")

        textTitulo = view.findViewById(R.id.textTituloLivro)
        textAutor = view.findViewById(R.id.textAutorLivro)
        textSobre = view.findViewById(R.id.textSobreLivro)
        textEditora = view.findViewById(R.id.textEditoraLivro)
        textIsbn13 = view.findViewById(R.id.textIsbn13Livro)
        textPaginas = view.findViewById(R.id.textPaginasLivro)
        imageLivroDetalhes = view.findViewById(R.id.imageLivroDetalhes)
        buttonApagarMidia = view.findViewById(R.id.buttonApagarMidia)

        if (livroTitulo != null) {
            carregarDetalhes(livroTitulo!!)
        } else {
            Toast.makeText(requireContext(), "Erro: Título do livro não encontrado.", Toast.LENGTH_SHORT).show()
            textTitulo.text = "Erro ao carregar"
        }

        buttonApagarMidia.setOnClickListener {
            confirmarExclusaoComPopup()
        }
    }

    private fun carregarDetalhes(tituloDoLivro: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Tenta buscar o livro na tabela "livros" usando o modelo Midia
                val listaDeLivros = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["livros"]
                        .select {
                            filter { eq("titulo", tituloDoLivro) }
                        }.decodeList<Midia>() // 🌟 Alterado para Midia aqui
                }

                val livro = listaDeLivros.firstOrNull()

                if (livro != null) {
                    // 🌟 O LIVRO EXISTE NO BANCO LOCAL (Mídia Própria cadastrada)
                    textTitulo.text = livro.titulo
                    textAutor.text = livro.autor
                    textSobre.text = "Mídia cadastrada no acervo local."
                    textEditora.text = "Interna"
                    textIsbn13.text = livro.isbn.ifEmpty { "--" }
                    textPaginas.text = "--" // O seu modelo Midia não guarda páginas, deixamos padrão

                    if (!livro.capaUrl.isNullOrEmpty()) {
                        Glide.with(requireContext()).load(livro.capaUrl).placeholder(R.drawable.user_placeholder).into(imageLivroDetalhes)
                    }
                } else {
                    // 🌟 O LIVRO VEIO DO GOOGLE BOOKS API (Busca na API online)
                    textTitulo.text = livroTitulo ?: "Título Indisponível"
                    textAutor.text = livroAutorExterna ?: "Autor Desconhecido"

                    if (!livroCapaExterna.isNullOrEmpty()) {
                        Glide.with(requireContext()).load(livroCapaExterna).placeholder(R.drawable.user_placeholder).into(imageLivroDetalhes)
                    }

                    textSobre.text = "Buscando detalhes do livro na internet..."

                    try {
                        val response = com.example.bibliounifornew.api.RetrofitClient.api.searchBooks(query = "intitle:\"${tituloDoLivro}\"")
                        val item = response.items?.firstOrNull()
                        val info = item?.volumeInfo

                        if (info != null) {
                            textSobre.text = info.description ?: "Sem descrição disponível na API do Google."
                            textIsbn13.text = info.industryIdentifiers?.firstOrNull()?.identifier ?: "Indisponível"
                            textEditora.text = "Google Books"
                            textPaginas.text = "N/I"
                        } else {
                            textSobre.text = "Detalhes adicionais não encontrados na base do Google Books."
                            textEditora.text = "--"
                            textIsbn13.text = "--"
                            textPaginas.text = "--"
                        }
                    } catch (e: Exception) {
                        textSobre.text = "Este livro foi alugado externamente via Google Books."
                        textEditora.text = "--"
                        textIsbn13.text = "--"
                        textPaginas.text = "--"
                    }

                    // Configura o botão como apenas leitura para mídias externas
                    buttonApagarMidia.isEnabled = false
                    buttonApagarMidia.text = "Mídia Externa (Apenas Leitura)"
                    buttonApagarMidia.setBackgroundColor(android.graphics.Color.GRAY)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar detalhes", Toast.LENGTH_LONG).show()
                textSobre.text = "Erro de conexão com o banco de dados."
            }
        }
    }

    private fun confirmarExclusaoComPopup() {
        val viewPopup = LayoutInflater.from(requireContext()).inflate(R.layout.popup_apagar_conta, null)

        val textTituloPopup = viewPopup.findViewById<TextView>(R.id.textTituloApagarConta)
        val editSenha = viewPopup.findViewById<EditText>(R.id.editSenhaPopup)
        val iconOlho = viewPopup.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)
        val textErro = viewPopup.findViewById<TextView>(R.id.textErroSenhaPopup)
        val btnConfirmar = viewPopup.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)

        textTituloPopup.text = "APAGAR MÍDIA?"

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(viewPopup)
        val dialog = builder.create()

        var senhaVisivel = false
        iconOlho.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editSenha.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            editSenha.setSelection(editSenha.text.length)
        }

        btnConfirmar.setOnClickListener {
            val senhaDigitada = editSenha.text.toString().trim()

            if (senhaDigitada == "admin123") {
                textErro.visibility = View.GONE
                dialog.dismiss()
                apagarLivroDoBanco()
            } else {
                textErro.visibility = View.VISIBLE
            }
        }

        dialog.show()
    }

    private fun apagarLivroDoBanco() {
        if (livroTitulo == null) return

        buttonApagarMidia.isEnabled = false
        buttonApagarMidia.text = "Apagando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["livros"].delete {
                        filter { eq("titulo", livroTitulo!!) }
                    }
                }
                Toast.makeText(requireContext(), "Mídia apagada com sucesso!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao apagar: ${e.message}", Toast.LENGTH_LONG).show()
                buttonApagarMidia.isEnabled = true
                buttonApagarMidia.text = "Apagar Mídia"
            }
        }
    }
}