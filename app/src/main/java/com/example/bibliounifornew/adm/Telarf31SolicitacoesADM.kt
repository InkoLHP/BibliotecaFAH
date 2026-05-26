package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.SolicitacaoAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Solicitacao
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class Telarf31SolicitacoesADM :
    Fragment(R.layout.telarf31_solicitacoes_adm) {

    private lateinit var recyclerSolicitacoes: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        recyclerSolicitacoes =
            view.findViewById(R.id.recyclerSolicitacoes)

        recyclerSolicitacoes.layoutManager =
            LinearLayoutManager(requireContext())

        carregarSolicitacoes()
    }

    private fun carregarSolicitacoes() {

        lifecycleScope.launch {

            val response = SupabaseConfig.client
                .from("solicitacoes")
                .select()

            val lista =
                response.decodeList<Solicitacao>()

            recyclerSolicitacoes.adapter =
                SolicitacaoAdapter(lista.toMutableList())
        }
    }
}