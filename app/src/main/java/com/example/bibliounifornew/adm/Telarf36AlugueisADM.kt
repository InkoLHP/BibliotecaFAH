package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback // 👇 IMPORTANTE
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*

class Telarf36AlugueisADM : Fragment(R.layout.telarf36_alugueis_adm) {

    private lateinit var recyclerAlugueis: RecyclerView
    private lateinit var adapter: AluguelAdapter
    private var listaAlugueis = mutableListOf<Aluguel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerAlugueis = view.findViewById(R.id.recyclerAlugueis)
        recyclerAlugueis.layoutManager = LinearLayoutManager(requireContext())

        adapter = AluguelAdapter(
            listaAlugueis = listaAlugueis,
            onVerLivroClick = { Toast.makeText(requireContext(), "Carregando info do livro...", Toast.LENGTH_SHORT).show() },
            onVerUsuarioClick = { aluguel -> Toast.makeText(requireContext(), "Acessando perfil de: ${aluguel.email_usuario}", Toast.LENGTH_SHORT).show() }
        )
        recyclerAlugueis.adapter = adapter

        // 👇 NOVO: Intercepta o botão de voltar do celular de forma segura nesta tela também
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