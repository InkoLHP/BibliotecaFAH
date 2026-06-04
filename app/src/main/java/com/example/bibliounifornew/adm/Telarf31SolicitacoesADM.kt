package com.example.bibliounifornew.adm

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
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

    private lateinit var solicitacaoAdapter: SolicitacaoAdapter
    private val listaInternaSolicitacoes = mutableListOf<Solicitacao>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🌟 NOVO: Recuperando a foto do ADM salva na sessão (SharedPreferences)
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val urlFoto = sharedPref.getString("USER_FOTO", null)

        // 🌟 NOVO: Mapeando a ImageView e carregando com o Coil
        val imageFotoPerfil = view.findViewById<ImageView>(R.id.imageFotoPerfilSolicitacoes)
        if (!urlFoto.isNullOrEmpty()) {
            imageFotoPerfil.load(urlFoto) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        recyclerSolicitacoes = view.findViewById(R.id.recyclerSolicitacoes)

        if (recyclerSolicitacoes == null) {
            Log.e("SOLICITACOES_ADM", "ERRO CRÍTICO: O ID 'recyclerSolicitacoes' não foi encontrado!")
            return
        }

        recyclerSolicitacoes?.layoutManager = LinearLayoutManager(requireContext())

        solicitacaoAdapter = SolicitacaoAdapter(
            listaInternaSolicitacoes
        ) { solicitacao, posicao ->

            lifecycleScope.launch {

                try {

                    withContext(Dispatchers.IO) {

                        SupabaseConfig.client
                            .from("solicitacoes")
                            .update(
                                {
                                    set("status", "CONCLUIDA")
                                }
                            ) {
                                filter {
                                    eq("id", solicitacao.id!!)
                                }
                            }

                        val timestampAtual = java.text.SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date())

                        val notificacao = Notificacao(
                            email_usuario = solicitacao.email_usuario,
                            titulo = "Solicitação Atendida",
                            mensagem = "Sua solicitação para '${solicitacao.titulo}' foi concluída.",
                            visualizada = false,
                            created_at = timestampAtual
                        )

                        SupabaseConfig.client
                            .from("notificacoes")
                            .insert(notificacao)
                    }

                    listaInternaSolicitacoes.removeAt(posicao)
                    solicitacaoAdapter.notifyItemRemoved(posicao)

                    Toast.makeText(
                        requireContext(),
                        "Solicitação concluída!",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {

                    e.printStackTrace()

                    Toast.makeText(
                        requireContext(),
                        e.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        recyclerSolicitacoes?.adapter = solicitacaoAdapter

        // Intercepta o botão de voltar do celular de forma segura
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

        carregarSolicitacoes()
    }

    private fun carregarSolicitacoes() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val listaDoBanco = withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .from("solicitacoes")
                        .select() {
                            filter {
                                eq("status", "PENDENTE")
                            }
                        }
                        .decodeList<Solicitacao>()
                }

                listaInternaSolicitacoes.clear()
                listaInternaSolicitacoes.addAll(listaDoBanco)
                solicitacaoAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("SOLICITACOES_ADM", "Erro ao carregar dados: ${e.message}")
                Toast.makeText(requireContext(), "Erro ao conectar com o servidor.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}