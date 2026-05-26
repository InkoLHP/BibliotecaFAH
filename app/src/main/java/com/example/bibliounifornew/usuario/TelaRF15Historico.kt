package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

        adapter = SolicitacaoAdapter(listaSolicitacoes)
        recyclerHistorico.adapter = adapter

        buscarHistorico()
    }

    private fun buscarHistorico() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
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

    // 👇 NOVO: Atualiza a foto se ela existir nessa tela
    override fun onResume() {
        super.onResume()
        val sharedPref = requireActivity().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
        val fotoUsuarioUri = sharedPref.getString("USER_FOTO", null)
        val profileImage = view?.findViewById<ImageView>(R.id.imagePerfilUsuario)

        if (profileImage != null && !fotoUsuarioUri.isNullOrBlank()) {
            Glide.with(this).load(fotoUsuarioUri).circleCrop().into(profileImage)
        }
    }
}