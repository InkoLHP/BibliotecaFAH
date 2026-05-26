package com.example.bibliounifornew.adm

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback // 👇 IMPORTANTE
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.SolicitacaoAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*

class Telarf31SolicitacoesADM : Fragment(R.layout.telarf31_solicitacoes_adm) {

    private var recyclerSolicitacoes: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerSolicitacoes = view.findViewById(R.id.recyclerSolicitacoes)

        if (recyclerSolicitacoes == null) {
            Log.e("SOLICITACOES_ADM", "ERRO CRÍTICO: O ID 'recyclerSolicitacoes' não foi encontrado!")
            return
        }

        recyclerSolicitacoes?.layoutManager = LinearLayoutManager(requireContext())

        // 👇 NOVO: Intercepta o botão de voltar do celular de forma segura
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Se houver telas no histórico, volta normalmente
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    // Se abriu pelo menu inferior (sem histórico), força a volta para o Dashboard
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, TelaRF28DashboardADM())
                        .commit()
                }
            }
        })

        carregarSolicitacoes()
    }

    private fun carregarSolicitacoes() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val lista = withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .from("solicitacoes")
                        .select()
                        .decodeList<Solicitacao>()
                }
                recyclerSolicitacoes?.adapter = SolicitacaoAdapter(lista.toMutableList())
            } catch (e: Exception) {
                Log.e("SOLICITACOES_ADM", "Erro ao carregar dados: ${e.message}")
                Toast.makeText(requireContext(), "Erro ao conectar com o servidor.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}