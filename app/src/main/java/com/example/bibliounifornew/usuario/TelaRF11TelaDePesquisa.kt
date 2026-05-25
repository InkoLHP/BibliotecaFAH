package com.example.bibliounifornew.usuario

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
        // 1. Bloqueia o botão e muda o texto para o usuário não clicar de novo
        buttonProcurar.isEnabled = false
        buttonProcurar.text = "Buscando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // CHAMA A API COM A SUA CHAVE
                val response = com.example.bibliounifornew.api.RetrofitClient
                    .api
                    .searchBooks(
                        query = pesquisa,
                        apiKey = "AIzaSyC8t_vTp_BNj82t6X1yWOX2dJkadMCT-1A" // SUBTITUA PELA SUA CHAVE REAL AQUI
                    )

                val livrosEncontrados = response.items?.map { item ->
                    Livro(
                        titulo = item.volumeInfo.title ?: "Sem título",
                        autor = item.volumeInfo.authors?.joinToString(", ") ?: "Autor desconhecido",
                        isbn = item.volumeInfo.industryIdentifiers?.firstOrNull()?.identifier ?: "Sem ISBN",
                        capaUrl = item.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://") ?: ""
                    )
                } ?: emptyList()

                // Se não encontrar nada, avisa o usuário
                if (livrosEncontrados.isEmpty()) {
                    Toast.makeText(requireContext(), "Nenhum livro encontrado.", Toast.LENGTH_SHORT).show()
                }

                // Atualiza o adapter usando o novo nome da classe
                recyclerLivros.adapter = LivroUsuarioAdapter(livrosEncontrados) { livro ->
                    abrirOpcoesLivro(livro)
                }

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    Toast.makeText(
                        requireContext(),
                        "Muitas buscas seguidas. Tente novamente em alguns segundos.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Erro no servidor do Google: ${e.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Erro de conexão. Verifique sua internet.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // 2. IMPORTANTE: Libera o botão novamente independente se deu erro ou sucesso
                buttonProcurar.isEnabled = true
                buttonProcurar.text = "Procurar"
            }
        }
    }

    private fun abrirOpcoesLivro(livro: Livro) {
        val titulo = livro.titulo
        val opcoes = arrayOf(
            "Ver detalhes",
            "Adicionar à Minha Livraria",
            "Adicionar à Lista de Desejos"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setItems(opcoes) { _, qual ->
                when (qual) {
                    0 -> Toast.makeText(requireContext(), "Abrindo detalhes de: $titulo", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(requireContext(), "$titulo adicionado à Livraria!", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext(), "$titulo adicionado aos Desejos!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun exibirPopupFiltros() {
        // Implementação futura dos filtros
    }
}