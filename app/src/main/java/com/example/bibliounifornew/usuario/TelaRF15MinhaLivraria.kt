package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.adapter.LivrariaAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.LivrariaItem
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF15MinhaLivraria : Fragment(R.layout.telarf15_minha_livraria) {

    private lateinit var recyclerLivraria: RecyclerView
    private lateinit var textEmail: TextView
    private lateinit var btnFiltroLivraria: ImageButton

    private var emailUsuario: String = ""
    private var listaOriginal: List<LivrariaItem> = emptyList()

    // Variável de controle para evitar cliques simultâneos ao deletar itens
    private var processandoClique: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerLivraria = view.findViewById(R.id.recyclerMinhaLivraria)
        textEmail = view.findViewById(R.id.textEmailLivraria)
        btnFiltroLivraria = view.findViewById(R.id.btnFiltroLivraria)

        recyclerLivraria.layoutManager = LinearLayoutManager(requireContext())

        // 🌟 CORRIGIDO: Sempre unificado no "user_session"
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailUsuario = sharedPref.getString("USER_EMAIL", "") ?: ""
        textEmail.text = emailUsuario

        btnFiltroLivraria.setOnClickListener { viewSeta ->
            mostrarMenuFiltro(viewSeta)
        }

        carregarLivrariaDoBanco()
    }

    private fun carregarLivrariaDoBanco() {
        if (emailUsuario.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val itensDoBanco = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["minha_livraria"]
                        .select {
                            filter { eq("email_usuario", emailUsuario) }
                        }.decodeList<LivrariaItem>()
                        .sortedByDescending { it.id }
                }

                listaOriginal = itensDoBanco
                configurarAdapter(listaOriginal)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar sua livraria", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarAdapter(listaParaExibir: List<LivrariaItem>) {
        recyclerLivraria.adapter = LivrariaAdapter(
            listaParaExibir,
            onCapaClick = { itemClicado ->
                abrirDetalhesDoLivro(itemClicado)
            },
            onRemoverClick = { itemParaRemover ->
                removerItemDoBanco(itemParaRemover)
            }
        )
    }

    // 🎯 SISTEMA DE FILTRO EM TEMPO REAL
    private fun mostrarMenuFiltro(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add(0, 1, 0, "Todos os Livros")
        popup.menu.add(0, 2, 1, "Não Lidos")
        popup.menu.add(0, 3, 2, "Lendo")
        popup.menu.add(0, 4, 3, "Lidos")

        popup.setOnMenuItemClickListener { menuItem ->
            // 🌟 CORRIGIDO: Mudado de "BiblioUniforPrefs" para "user_session" para ler a gravação correta
            val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)

            val listaFiltrada = when (menuItem.itemId) {
                2 -> listaOriginal.filter { sharedPrefs.getString("status_${it.livro_id}", "NAO_LIDO") == "NAO_LIDO" }
                3 -> listaOriginal.filter { sharedPrefs.getString("status_${it.livro_id}", "NAO_LIDO") == "LENDO" }
                4 -> listaOriginal.filter { sharedPrefs.getString("status_${it.livro_id}", "NAO_LIDO") == "LIDO" }
                else -> listaOriginal
            }

            configurarAdapter(listaFiltrada)
            true
        }
        popup.show()
    }

    private fun abrirDetalhesDoLivro(item: LivrariaItem) {
        // Convertemos o LivrariaItem em um objeto Livro para a tela de detalhes entender
        val livroMapeado = com.example.bibliounifornew.model.Livro(
            id = item.livro_id ?: 0,
            titulo = item.titulo,
            autor = item.autor,
            isbn = "Sem ISBN",
            capaUrl = item.capa_url ?: "",
            sinopse = "Carregado da sua livraria local.",
            data_publicacao = null,
            categoria = item.categoria,
            formato = "Físico",
            disponivel = true,
            pdfUrl = null
        )

        // Abre a Activity de detalhes passando o livro
        val intent = Intent(requireContext(), TelaRF12TelaDoLivro::class.java).apply {
            putExtra("livro", livroMapeado)
        }
        startActivity(intent)
    }

    // 🗑️ DELETAR DA LIVRARIA (PROTEGIDA)
    private fun removerItemDoBanco(item: LivrariaItem) {
        if (processandoClique) return
        processandoClique = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["minha_livraria"]
                        .delete {
                            filter { eq("id", item.id ?: 0) }
                        }
                }
                Toast.makeText(requireContext(), "${item.titulo} removido!", Toast.LENGTH_SHORT).show()
                carregarLivrariaDoBanco()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao remover livro", Toast.LENGTH_SHORT).show()
            } finally {
                processandoClique = false // 🔓 Libera para novas remoções
            }
        }
    }
}