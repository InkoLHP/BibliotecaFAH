package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import com.example.bibliounifornew.model.Livro

class TelaRF11TelaDePesquisa : Fragment(R.layout.telarf11_tela_pesquisa) {

    private lateinit var recyclerLivros: RecyclerView
    private lateinit var editPesquisarLivro: EditText
    private lateinit var buttonProcurar: MaterialButton
    private lateinit var iconFiltro: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MAPEAMENTO (RF11.1)
        recyclerLivros = view.findViewById(R.id.recyclerLivros)
        editPesquisarLivro = view.findViewById(R.id.editPesquisarLivro)
        buttonProcurar = view.findViewById(R.id.buttonProcurar)
        iconFiltro = view.findViewById(R.id.iconFiltro)

        recyclerLivros.layoutManager = LinearLayoutManager(requireContext())

        // CORRIGIDO: Inicializa o adapter vazio com o novo nome
        recyclerLivros.adapter = LivroUsuarioAdapter(emptyList<Livro>()) { livro ->
            abrirOpcoesLivro(livro)
        }

        // RF11.5 - Botão Procurar fazendo a busca real na Internet
        buttonProcurar.setOnClickListener {
            val pesquisa = editPesquisarLivro.text.toString().trim()

            if (pesquisa.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Digite um título ou autor",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            buscarLivros(pesquisa)
        }

        // RF11.2, RF11.3, RF11.4 - Abre o Pop-up de Filtros Avançados
        iconFiltro.setOnClickListener {
            exibirPopupFiltros()
        }
    }

    private fun buscarLivros(pesquisa: String) {
        // 1. Bloqueia o botão e muda o texto
        buttonProcurar.isEnabled = false
        buttonProcurar.text = "Buscando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // CHAMA A API (Nota: apiKey removida daqui, pois o seu Interceptor já injeta ela!)
                val response = com.example.bibliounifornew.api.RetrofitClient
                    .api
                    .searchBooks(query = pesquisa)

                // Mapeamento COMPLETO para a TelaRF12 ter todos os dados
                val livrosEncontrados = response.items?.map { item ->
                    val info = item.volumeInfo

                    // Tratamento seguro dos ISBNs
                    val isbn13 = info.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier
                    val isbn10 = info.industryIdentifiers?.find { it.type == "ISBN_10" }?.identifier
                    val isbnFinal = isbn13 ?: isbn10 ?: "Sem ISBN"

                    Livro(
                        id = item.id, // Fundamental para os marcadores de leitura (Lido, Lendo...)
                        titulo = info.title ?: "Sem título",
                        autor = info.authors?.joinToString(", ") ?: "Autor desconhecido",
                        isbn = isbnFinal,
                        capaUrl = info.imageLinks?.thumbnail?.replace("http://", "https://") ?: "",
                        sinopse = info.description,
                        data_publicacao = info.publishedDate,
                        categoria = info.categories?.firstOrNull(),
                        formato = "Físico", // Padronizado
                        disponivel = (0..1).random() == 1,
                        pdfUrl = info.previewLink?.replace("http://", "https://")
                    )
                } ?: emptyList()

                // Se não encontrar nada, avisa o usuário
                if (livrosEncontrados.isEmpty()) {
                    Toast.makeText(requireContext(), "Nenhum livro encontrado.", Toast.LENGTH_SHORT).show()
                }

                // Atualiza o adapter
                recyclerLivros.adapter = LivroUsuarioAdapter(livrosEncontrados) { livro ->
                    // Opcional: Se quiser que o clique vá DIRETO pra tela do livro, chame a Intent aqui.
                    // Como você tem um menu de opções, vamos manter abrindo as opções:
                    abrirOpcoesLivro(livro)
                }

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    Toast.makeText(requireContext(), "Muitas buscas seguidas. Tente em alguns segundos.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Erro no servidor: ${e.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro de conexão. Verifique sua internet.", Toast.LENGTH_LONG).show()
            } finally {
                // 2. Libera o botão
                buttonProcurar.isEnabled = true
                buttonProcurar.text = "Procurar"
            }
        }
    }

    private fun abrirOpcoesLivro(livro: Livro) {
        val titulo = livro.titulo

        val intent = Intent(requireContext(), TelaRF12TelaDoLivro::class.java)
        intent.putExtra("livro", livro) // Manda o livro clicado
        startActivity(intent)
    }

    private fun exibirPopupFiltros() {
        // Implementação futura dos filtros
    }
}