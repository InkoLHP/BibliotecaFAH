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
import com.example.bibliounifornew.Adapter.HistoricoAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.Solicitacao
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val emailLogado = sharedPref.getString("USER_EMAIL", "") ?: ""
        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)

        textEmailHistorico.text = emailLogado

        if (!fotoSalvaUrl.isNullOrEmpty()) {
            try {
                imagePerfilHistorico.setImageURI(Uri.parse(fotoSalvaUrl))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        carregarDadosUnificados(emailLogado)
    }

    private fun carregarDadosUnificados(email: String) {
        if (email.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 🌟 BUSCA PARALELA: Puxa aluguéis e solicitações ao mesmo tempo
                val dadosUnificados = withContext(Dispatchers.IO) {
                    val buscaAlugueis = async {
                        SupabaseConfig.client.postgrest["alugueis"]
                            .select {
                                filter {
                                    eq("email_usuario", email)
                                    neq("oculto_historico", true)
                                }
                            }.decodeList<Aluguel>()
                    }

                    val buscaSolicitacoes = async {
                        SupabaseConfig.client.postgrest["solicitacoes"]
                            .select {
                                filter {
                                    eq("email_usuario", email)
                                    // Se tiver um campo de ocultar na solicitação futuramente, filtra aqui
                                }
                            }.decodeList<Solicitacao>()
                    }

                    val listaAlugueis = buscaAlugueis.await()
                    val listaSolicitacoes = buscaSolicitacoes.await()

                    // Converte as solicitações em um formato que o seu HistoricoAdapter entenda como "Card"
                    // sem alterar a estrutura visual existente.
                    val itensConvertidos = listaSolicitacoes.map { sol ->
                        Aluguel(
                            id = sol.id,
                            email_usuario = sol.email_usuario,
                            titulo_livro = sol.titulo,
                            autor_livro = sol.autor,
                            capa_url = sol.capa_url,
                            data_vencimento = "Status: ${sol.status}",
                            dias_restantes = 0,
                            devolvido = false,
                            oculto_historico = false
                        )
                    }

                    // Junta as duas listas e ordena tudo junto pelo ID (Mais recentes no topo)
                    (listaAlugueis + itensConvertidos).sortedByDescending { it.id }
                }

                // Adapta o clique de remoção padrão para identificar se veio de aluguel ou solicitação
                recyclerHistorico.adapter = HistoricoAdapter(dadosUnificados) { itemClicado ->
                    if (itemClicado.data_vencimento.startsWith("Status:")) {
                        removerSolicitacaoDoHistorico(itemClicado.id ?: 0, email)
                    } else {
                        removerAluguelDoBanco(itemClicado, email)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar dados unificados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removerAluguelDoBanco(aluguel: Aluguel, email: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"]
                        .update({ set("oculto_historico", true) }) {
                            filter { eq("id", aluguel.id ?: 0) }
                        }
                }
                Toast.makeText(requireContext(), "Item ocultado do histórico", Toast.LENGTH_SHORT).show()
                carregarDadosUnificados(email)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun removerSolicitacaoDoHistorico(idSolicitacao: Int, email: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    // Aqui você pode dar um delete real na solicitação se quiser sumir com ela de vez
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