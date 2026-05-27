package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.DesejosAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.DesejoItem
import com.example.bibliounifornew.model.LivrariaItem
import com.example.bibliounifornew.model.Livro
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF16ListaDesejos : Fragment(R.layout.telarf16_lista_desejos) {

    private lateinit var recyclerDesejos: RecyclerView
    private var emailUsuario: String = ""
    private var listaItens: MutableList<DesejoItem> = mutableListOf()
    private lateinit var adapterDesejos: DesejosAdapter

    // 🛡️ Variável de controle para travar cliques simultâneos nesta tela também!
    private var processandoClique: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lendo do arquivo correto e unificado
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailUsuario = sharedPref.getString("USER_EMAIL", "") ?: ""

        recyclerDesejos = view.findViewById(R.id.recyclerListaDesejos)
        recyclerDesejos.layoutManager = LinearLayoutManager(requireContext())

        if (emailUsuario.isNotEmpty()) {
            carregarListaDesejos()
        } else {
            Toast.makeText(requireContext(), "Usuário não identificado. Faça login novamente.", Toast.LENGTH_LONG).show()
        }
    }

    private fun carregarListaDesejos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["lista_desejos"]
                        .select {
                            filter {
                                eq("email_usuario", emailUsuario)
                            }
                        }.decodeList<DesejoItem>()
                }

                listaItens = resultado.toMutableList()

                if (listaItens.isEmpty()) {
                    Toast.makeText(requireContext(), "Sua lista de desejos está vazia! 🛒", Toast.LENGTH_SHORT).show()
                }

                adapterDesejos = DesejosAdapter(
                    itens = listaItens,
                    onCapaClick = { item -> abrirDetalhesDoLivro(item) },
                    onRemoverClick = { item -> removerDosDesejos(item) },
                    onLivrariaClick = { item -> moverParaLivraria(item) },
                    onAlugarClick = { item -> alugarLivro(item) } // 🌟 Protegido lá embaixo
                )
                recyclerDesejos.adapter = adapterDesejos

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar lista de desejos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🗑️ AÇÃO: DELETAR DA LISTA DE DESEJOS (PROTEGIDA)
    private fun removerDosDesejos(item: DesejoItem) {
        if (processandoClique) return
        processandoClique = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["lista_desejos"].delete {
                        filter { eq("id", item.id ?: 0) }
                    }
                }
                Toast.makeText(requireContext(), "${item.titulo} removido!", Toast.LENGTH_SHORT).show()
                carregarListaDesejos()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao remover", Toast.LENGTH_SHORT).show()
            } finally {
                processandoClique = false // 🔓 Libera
            }
        }
    }

    // 📚 AÇÃO: MOVER PARA LIVRARIA (PROTEGIDA)
    private fun moverParaLivraria(item: DesejoItem) {
        if (processandoClique) return
        processandoClique = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val novoItemLivraria = LivrariaItem(
                    id = null,
                    email_usuario = emailUsuario,
                    livro_id = item.livro_id,
                    titulo = item.titulo,
                    autor = item.autor,
                    capa_url = item.capa_url,
                    categoria = item.categoria
                )

                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["minha_livraria"].insert(novoItemLivraria)
                    SupabaseConfig.client.postgrest["lista_desejos"].delete {
                        filter { eq("id", item.id ?: 0) }
                    }
                }

                Toast.makeText(requireContext(), "Movido para a Minha Livraria! 📚", Toast.LENGTH_SHORT).show()
                carregarListaDesejos()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao mover livro", Toast.LENGTH_SHORT).show()
            } finally {
                processandoClique = false // 🔓 Libera
            }
        }
    }

    // 🔑 AÇÃO: ALUGAR LIVRO (PROTEGIDA)
    private fun alugarLivro(item: DesejoItem) {
        // 🛑 Se o usuário clicar enquanto processa o aluguel anterior, ignora!
        if (processandoClique) return
        processandoClique = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Simulando o tempo de processamento de rede/banco
                withContext(Dispatchers.IO) {
                    // Aqui entrará seu código futuro do Supabase para alugar
                    // Ex: SupabaseConfig.client.postgrest["alugueis"].insert(...)
                }

                Toast.makeText(requireContext(), "Processando aluguel de: ${item.titulo}", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao processar aluguel", Toast.LENGTH_SHORT).show()
            } finally {
                // 🔓 Libera a tela para o usuário interagir com outros livros após o término
                processandoClique = false
            }
        }
    }

    private fun abrirDetalhesDoLivro(item: DesejoItem) {
        val livroMapeado = Livro(
            id = item.livro_id ?: 0,
            titulo = item.titulo,
            autor = item.autor,
            isbn = "Sem ISBN",
            capaUrl = item.capa_url ?: "",
            sinopse = "",
            data_publicacao = null,
            categoria = item.categoria,
            formato = "Físico",
            disponivel = item.disponivel ?: true,
            pdfUrl = null
        )

        val intent = Intent(requireContext(), TelaRF12TelaDoLivro::class.java).apply {
            putExtra("livro", livroMapeado)
        }
        startActivity(intent)
    }
}