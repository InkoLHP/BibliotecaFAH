package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.NotificacaoAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Notificacao
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TelaRF14Notificacoes :
    Fragment(R.layout.telarf14_notificacoes) {

    private lateinit var recycler: RecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(
            R.id.recyclerNotificacoes
        )

        recycler.layoutManager =
            LinearLayoutManager(requireContext())

        carregarNotificacoes()
    }

    private fun carregarNotificacoes() {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val lista = SupabaseConfig.client
                    .from("notificacoes")
                    .select()
                    .decodeList<Notificacao>()

                CoroutineScope(Dispatchers.Main).launch {

                    recycler.adapter =
                        NotificacaoAdapter(lista)
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }
}