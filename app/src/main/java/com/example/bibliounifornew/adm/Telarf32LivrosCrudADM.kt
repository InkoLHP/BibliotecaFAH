package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Molde para RECEBER os dados do Supabase
@Serializable
data class LivroCadastrado(
    val id: String,
    val titulo: String? = null,
    val autor: String? = null,
    val isbn: String? = null,
    @SerialName("capaUrl") val capaUrl: String? = null
)

class Telarf32LivrosCrudADM : Fragment(R.layout.telarf32_livros_crud_adm) {

    private lateinit var adapter: LivrosAdmAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar o botão de "Adicionar Nova Mídia"
        val btnAdicionarMidia = view.findViewById<MaterialButton>(R.id.btnAdicionarMidia)
        btnAdicionarMidia.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, TelaRF33CadastroDeLivros())
                .addToBackStack(null)
                .commit()
        }

        // 2. Configurar o RecyclerView
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerLivrosAdm)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Configura o adapter injetando a lógica de clique para enviar o ID
        adapter = LivrosAdmAdapter(emptyList()) { livroClicado ->

            // AQUI ESTÁ A MUDANÇA: Criamos o pacote com o ID do livro clicado
            val argumentos = Bundle().apply {
                putString("LIVRO_ID", livroClicado.id)
            }

            // Criamos a tela 37 e colocamos os argumentos nela
            val telaDetalhes = TelaRF37EditarMidia().apply {
                arguments = argumentos
            }

            // Fazemos a substituição da tela usando a instância que criamos acima
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, telaDetalhes) // <-- Usando a variável com o ID dentro
                .addToBackStack(null)
                .commit()
        }
        recycler.adapter = adapter
        // 3. Buscar os livros no Supabase
        buscarLivrosDoBanco()
    }

    private fun buscarLivrosDoBanco() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Vai buscar na internet em segundo plano
                val listaDeLivros = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["livros"]
                        .select()
                        .decodeList<LivroCadastrado>()
                }

                // Atualiza a tela com os livros encontrados
                adapter.atualizarLista(listaDeLivros)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar livros.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}