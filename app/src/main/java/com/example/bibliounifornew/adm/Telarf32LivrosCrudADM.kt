package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.LivrosAdmAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LivroCadastrado(
    @SerialName("id")
    val id: Long,

    @SerialName("titulo")
    val titulo: String? = null,

    @SerialName("autor")
    val autor: String? = null,

    @SerialName("isbn")
    val isbn: String? = null,

    @SerialName("capaUrl")
    val capaUrl: String? = null
)

class Telarf32LivrosCrudADM : Fragment(R.layout.telarf32_livros_crud_adm) {

    private lateinit var adapter: LivrosAdmAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, TelaRF28DashboardADM())
                        .commit()
                }
            }
        })

        // 1. Configurar o botão de "Adicionar Nova Mídia"
        val btnAdicionarMidia = view.findViewById<MaterialButton>(R.id.btnAdicionarMidia)
        btnAdicionarMidia.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, TelaRF33CadastroDeLivros())
                .addToBackStack(null)
                .commit()
        }

        // 2. Configurar o RecyclerView
        // 2. Configurar o RecyclerView
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerLivrosAdm)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Configura o adapter injetando a lógica de clique para enviar o ID
        // 💡 Certifique-se se o seu adapter usa "LivroCadastrado" ou "Midia"
        adapter = LivrosAdmAdapter(emptyList()) { livroClicado ->
            val argumentos = Bundle().apply {
                putLong("LIVRO_ID", livroClicado.id)
            }

            val telaDetalhes = TelaRF37EditarMidia().apply {
                arguments = argumentos
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, telaDetalhes)
                .addToBackStack(null)
                .commit()
        }
        recycler.adapter = adapter

        buscarLivrosDoBanco()
    }

    private fun buscarLivrosDoBanco() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val listaDeLivros = withContext(Dispatchers.IO) {
                    // 1. Faz a busca no Supabase (retorna um PostgrestResult)
                    val resultado = SupabaseConfig.client.postgrest["livros"].select()

                    val jsonBruto = resultado.data

                    // 3. Configura o JSON do Kotlin para ignorar colunas extras e aceitar flexibilidades
                    val jsonConfig = kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true  // Ignora colunas que você não mapeou (ex: exemplares, editora)
                        coerceInputValues = true  // Previne falhas se vier algo inesperado
                        isLenient = true          // Permite leitura flexível de strings/literais
                    }

                    // 4. Converte manualmente da String para a lista de objetos LivroCadastrado
                    jsonConfig.decodeFromString<List<LivroCadastrado>>(jsonBruto)
                }

                // Alimenta o adapter com a lista convertida
                adapter.atualizarLista(listaDeLivros)

                if (listaDeLivros.isEmpty()) {
                    Toast.makeText(requireContext(), "Tabela de livros vazia.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                val erroReal = e.localizedMessage ?: e.message ?: "Erro desconhecido"

                // Mostra o erro real exato se ainda houver alguma falha de conversão
                Toast.makeText(requireContext(), "Erro de leitura: $erroReal", Toast.LENGTH_LONG).show()
            }
        }
    }
}