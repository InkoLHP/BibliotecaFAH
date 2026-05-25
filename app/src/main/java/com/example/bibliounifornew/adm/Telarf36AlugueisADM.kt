package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.Aluguel
import com.example.bibliounifornew.data.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Telarf36AlugueisADM : Fragment(R.layout.telarf36_alugueis_adm) {

    private lateinit var recyclerAlugueis: RecyclerView
    private lateinit var adapter: AluguelAdapter
    private var listaAlugueis = mutableListOf<Aluguel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerAlugueis = view.findViewById(R.id.recyclerAlugueis)
        recyclerAlugueis.layoutManager = LinearLayoutManager(requireContext())

        // Inicializa o Adapter e os cliques dos botões
        adapter = AluguelAdapter(
            listaAlugueis = listaAlugueis,
            onVerLivroClick = { aluguel ->
                Toast.makeText(requireContext(), "Carregando info do livro...", Toast.LENGTH_SHORT).show()
                // Futuramente: Navegar para RF37 Editar Livro passando o nome/autor
            },
            onVerUsuarioClick = { aluguel ->
                Toast.makeText(requireContext(), "Acessando perfil de: ${aluguel.emailUsuario}", Toast.LENGTH_SHORT).show()
                // Futuramente: Navegar para o Perfil do Usuário
            }
        )
        recyclerAlugueis.adapter = adapter

        // Busca os dados
        buscarAlugueisAtivos()
    }

    private fun buscarAlugueisAtivos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val alugueisDoBanco = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"]
                        .select {
                            filter { eq("devolvido", false) } // Só puxa quem NÃO devolveu
                        }
                        .decodeList<Aluguel>()
                }

                listaAlugueis.clear()
                listaAlugueis.addAll(alugueisDoBanco)
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao buscar dados do Supabase", Toast.LENGTH_SHORT).show()
            }
        }
    }
}