package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.adapter.AluguelADMAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*

class Telarf36AlugueisADM : Fragment(R.layout.telarf36_alugueis_adm) {

    private lateinit var recyclerAlugueis: RecyclerView
    private lateinit var adapter: AluguelADMAdapter
    private var listaAlugueis = mutableListOf<Aluguel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerAlugueis = view.findViewById(R.id.recyclerAlugueis)
        recyclerAlugueis.layoutManager = LinearLayoutManager(requireContext())

        // Configuração do Adapter com as ações reais de clique
        adapter = AluguelADMAdapter(
            listaAlugueis = listaAlugueis,
            onVerLivroClick = { aluguel ->
                // 1. Criamos o Fragment de Edição de Mídia
                val fragment = TelaRF37EditarMidia().apply {
                    arguments = Bundle().apply {
                        putString("LIVRO_TITULO", aluguel.titulo_livro)
                    }
                }

                // 2. Transição de tela jogando o fragment na pilha de volta (BackStack)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onVerUsuarioClick = { aluguel ->
                val fragment = Telarf30UsuarioAlugadosADM().apply {
                    arguments = Bundle().apply {
                        putString("email", aluguel.email_usuario)
                        putString("nome", "Estudante") // Nome padrão caso precise
                    }
                }

                // 2. Transição para a tela de gerenciamento de usuários
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
        recyclerAlugueis.adapter = adapter

        // Interceptador do botão de voltar físico do aparelho
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

        buscarAlugueisAtivos()
    }

    private fun buscarAlugueisAtivos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val alugueisDoBanco = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"]
                        .select { filter { eq("devolvido", false) } }
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