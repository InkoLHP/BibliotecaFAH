package com.example.bibliounifornew.usuario

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.adapter.HistoricoAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*

class TelaRF15Historico : Fragment(R.layout.telarf15_historico) {

    private lateinit var recyclerHistorico: RecyclerView
    private lateinit var textEmailHistorico: TextView
    private lateinit var imagePerfilHistorico: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerHistorico = view.findViewById(R.id.recyclerHistorico)
        textEmailHistorico = view.findViewById(R.id.textEmailHistorico)
        imagePerfilHistorico = view.findViewById(R.id.imagePerfilHistorico)

        recyclerHistorico.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)

        val emailLogado = sharedPref.getString("USER_EMAIL", "")?.lowercase()?.trim() ?: ""
        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)

        textEmailHistorico.text = emailLogado

        if (!fotoSalvaUrl.isNullOrEmpty()) {
            imagePerfilHistorico.load(fotoSalvaUrl) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        carregarDadosUnificados(emailLogado)
    }

    private fun carregarDadosUnificados(email: String) {
        if (email.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dadosUnificados = withContext(Dispatchers.IO) {
                    val buscaAlugueis = async {
                        try {
                            SupabaseConfig.client.postgrest["alugueis"]
                                .select {
                                    filter {
                                        eq("email_usuario", email)
                                    }
                                }.decodeList<Aluguel>()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList<Aluguel>()
                        }
                    }

                    val buscaSolicitacoes = async {
                        try {
                            SupabaseConfig.client.postgrest["solicitacoes"]
                                .select {
                                    filter {
                                        eq("email_usuario", email)
                                    }
                                }.decodeList<Solicitacao>()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList<Solicitacao>()
                        }
                    }

                    // CORREÇÃO: Usando '== true' para tratar Boolean? e '?: ""' para String?
                    val listaAlugueis = buscaAlugueis.await().filter { it.oculto_historico != true }
                    val listaSolicitacoes = buscaSolicitacoes.await()

                    val itensConvertidos = listaSolicitacoes.map { sol ->
                        Aluguel(
                            id = sol.id,
                            email_usuario = sol.email_usuario,
                            titulo_livro = sol.titulo,
                            autor_livro = sol.autor,
                            capa_url = sol.capa_url,
                            data_vencimento = "Status: ${sol.status}",
                            dias_restantes = 0L,
                            devolvido = false,
                            oculto_historico = false
                        )
                    }

                    (listaAlugueis + itensConvertidos).sortedByDescending { it.id ?: 0L }
                }

                recyclerHistorico.adapter = HistoricoAdapter(dadosUnificados) { itemClicado ->
                    // CORREÇÃO: Pegando o valor de forma segura para usar o startsWith
                    val vencimentoSeguro = itemClicado.data_vencimento ?: ""

                    if (vencimentoSeguro.startsWith("Status:")) {
                        removerSolicitacaoDoHistorico(itemClicado.id ?: 0L, email)
                    } else {
                        removerAluguelDoBanco(itemClicado, email)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro Histórico: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removerAluguelDoBanco(aluguel: Aluguel, email: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"]
                        .update({ set("oculto_historico", true) }) {
                            filter { eq("id", aluguel.id ?: 0L) }
                        }
                }
                Toast.makeText(requireContext(), "Item ocultado do histórico", Toast.LENGTH_SHORT).show()
                carregarDadosUnificados(email)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun removerSolicitacaoDoHistorico(idSolicitacao: Long, email: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["solicitacoes"]
                        .delete {
                            filter { eq("id", idSolicitacao) }
                        }
                }
                Toast.makeText(requireContext(), "Solicitação removida", Toast.LENGTH_SHORT).show()
                carregarDadosUnificados(email)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}