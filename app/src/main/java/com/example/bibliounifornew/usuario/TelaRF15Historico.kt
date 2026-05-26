package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.SolicitacaoAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Solicitacao
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF15Historico : Fragment(R.layout.telarf15_historico) {

    private lateinit var recyclerHistorico: RecyclerView
    private lateinit var adapter: SolicitacaoAdapter
    private var listaSolicitacoes = mutableListOf<Solicitacao>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerHistorico = view.findViewById(R.id.recyclerHistorico)
        recyclerHistorico.layoutManager = LinearLayoutManager(requireContext())

        // Passa a lista pro adapter do seu grupo
        adapter = SolicitacaoAdapter(listaSolicitacoes)
        recyclerHistorico.adapter = adapter

        buscarHistorico()
    }

    private fun buscarHistorico() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Puxa do Supabase
                val dadosDoBanco = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["solicitacoes"]
                        .select()
                        .decodeList<Solicitacao>()
                }

                listaSolicitacoes.clear()
                listaSolicitacoes.addAll(dadosDoBanco)
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao buscar histórico", Toast.LENGTH_SHORT).show()
            }
        }
    }
}