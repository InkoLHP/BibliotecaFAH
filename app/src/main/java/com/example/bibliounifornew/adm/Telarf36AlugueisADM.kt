package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.AluguelADMAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.Livro
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Telarf36AlugueisADM : Fragment(R.layout.telarf36_alugueis_adm) {

    private lateinit var recyclerAlugueis: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa o RecyclerView com o ID do XML telarf36_alugueis_adm
        recyclerAlugueis = view.findViewById(R.id.recyclerAlugueis)
        recyclerAlugueis.layoutManager = LinearLayoutManager(requireContext())

        carregarTodosAlugueis()
    }

    private fun carregarTodosAlugueis() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val lista = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("alugueis").select().decodeList<Aluguel>()
                }

                recyclerAlugueis.adapter = AluguelADMAdapter(
                    listaAlugueis = lista,
                    onVerLivroClick = { aluguel ->
                        // Navega para os detalhes do livro
                        val livro = Livro(
                            id = aluguel.id_livro ?: "",
                            titulo = aluguel.titulo_livro ?: "",
                            autor = aluguel.autor_livro ?: "",
                            isbn = "",
                            capaUrl = aluguel.capa_url ?: "",
                            sinopse = "Visualizado via Gestão de Aluguéis",
                            data_publicacao = null,
                            categoria = null,
                            formato = "Físico",
                            disponivel = false,
                            pdfUrl = null
                        )
                        val telaDetalhes = TelaRF12TelaDoLivroADM().apply {
                            arguments = Bundle().apply { putSerializable("livro", livro) }
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, telaDetalhes)
                            .addToBackStack(null)
                            .commit()
                    },
                    onVerUsuarioClick = { aluguel ->
                        Toast.makeText(requireContext(), "Usuário: ${aluguel.email_usuario}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar aluguéis: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}