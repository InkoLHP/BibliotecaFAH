package com.example.bibliounifornew.usuario

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.Adapter.AluguelAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.Notificacao
import com.example.bibliounifornew.model.Solicitacao
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF13StatusAluguel : Fragment(R.layout.telarf13_status) {

    private lateinit var recyclerAlugueis: RecyclerView
    private lateinit var textNenhumLivro: TextView

    private var emailUsuario: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerAlugueis = view.findViewById(R.id.recyclerAlugueis)
        textNenhumLivro = view.findViewById(R.id.textNenhumLivro)

        recyclerAlugueis.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailUsuario = sharedPref.getString("USER_EMAIL", "") ?: ""

        carregarStatusUnificado()
    }

    private fun carregarStatusUnificado() {
        if (emailUsuario.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 🌟 BUSCA UNIFICADA PARALELA: Aluguéis ativos + Solicitações
                val listaCompleta = withContext(Dispatchers.IO) {
                    val buscaAlugueis = async {
                        SupabaseConfig.client.postgrest["alugueis"]
                            .select {
                                filter {
                                    eq("email_usuario", emailUsuario)
                                    // Apenas aluguéis que ainda não foram marcados como devolvidos
                                    eq("devolvido", false)
                                }
                            }.decodeList<Aluguel>()
                    }

                    val buscaSolicitacoes = async {
                        SupabaseConfig.client.postgrest["solicitacoes"]
                            .select {
                                filter {
                                    eq("email_usuario", emailUsuario)
                                }
                            }.decodeList<Solicitacao>()
                    }

                    val alugueisAtivos = buscaAlugueis.await()
                    val solicitacoesAbertas = buscaSolicitacoes.await()

                    // Converte as solicitações para a estrutura do card dinâmico do AluguelAdapter
                    val solicitacoesConvertidas = solicitacoesAbertas.map { sol ->
                        Aluguel(
                            id = sol.id,
                            email_usuario = sol.email_usuario,
                            titulo_livro = sol.titulo,
                            autor_livro = sol.autor,
                            capa_url = sol.capa_url,
                            data_vencimento = "Status: ${sol.status}", // Injeta o gatilho "Status:" para o Adapter saber
                            // Atalho: usamos 1 para PDF_DIGITAL e 2 para LIVRO_FISICO (ou outros) pro Adapter ajustar os textos
                            dias_restantes = if (sol.tipo_solicitacao == "PDF_DIGITAL") 1 else 2,
                            devolvido = false
                        )
                    }

                    // Junta as listas colocando os IDs mais novos no topo
                    (alugueisAtivos + solicitacoesConvertidas).sortedByDescending { it.id }
                }

                if (listaCompleta.isEmpty()) {
                    textNenhumLivro.visibility = View.VISIBLE
                    textNenhumLivro.text = "Nenhum aluguel ou solicitação encontrada."
                    recyclerAlugueis.visibility = View.GONE
                } else {
                    textNenhumLivro.visibility = View.GONE
                    recyclerAlugueis.visibility = View.VISIBLE

                    // Configura o adapter com a nossa lógica de cliques do novo botão
                    recyclerAlugueis.adapter = AluguelAdapter(listaCompleta) { itemClicado, ehSolicitacao ->
                        if (ehSolicitacao) {
                            cancelarSolicitacaoReal(itemClicado.id ?: 0, itemClicado.titulo_livro)
                        } else {
                            cancelarAluguelReal(itemClicado, emailUsuario)
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                textNenhumLivro.visibility = View.VISIBLE
                textNenhumLivro.text = "Erro ao carregar dados de status."
            }
        }
    }

    private fun cancelarAluguelReal(aluguel: Aluguel, emailUsuarioLogado: String) {
        val dataHoraAtual = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.getDefault())
            .format(java.util.Date())

        val notificacaoParaAdmin = Notificacao(
            email_usuario = "admin@biblioteca.com",
            titulo = "Aluguel Cancelado 🚨",
            mensagem = "O usuário '$emailUsuarioLogado' cancelou o aluguel do livro '${aluguel.titulo_livro}'.",
            visualizada = false,
            created_at = dataHoraAtual
        )

        // 🌟 Corrigido: Trocado GlobalScope para viewLifecycleOwner.lifecycleScope para evitar vazamentos de memória
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    // 1. Apaga permanentemente da tabela de aluguéis
                    SupabaseConfig.client.postgrest["alugueis"].delete {
                        filter { eq("id", aluguel.id ?: 0) }
                    }
                    // 2. Insere a notificação avisando o admin
                    SupabaseConfig.client.postgrest["notificacoes"].insert(notificacaoParaAdmin)
                }

                Toast.makeText(requireContext(), "Aluguel cancelado e administrador notificado! 👍", Toast.LENGTH_LONG).show()
                // Atualiza a tela de forma limpa
                carregarStatusUnificado()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao processar cancelamento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelarSolicitacaoReal(idSolicitacao: Int, tituloLivro: String) {
        val dataHoraAtual = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.getDefault())
            .format(java.util.Date())

        val notificacaoParaAdmin = Notificacao(
            email_usuario = "admin@biblioteca.com",
            titulo = "Solicitação Cancelada 🛑",
            mensagem = "O usuário '$emailUsuario' cancelou a solicitação pendente do livro '$tituloLivro'.",
            visualizada = false,
            created_at = dataHoraAtual
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    // 1. Remove da tabela de solicitações
                    SupabaseConfig.client.postgrest["solicitacoes"].delete {
                        filter { eq("id", idSolicitacao) }
                    }
                    // 2. Notifica o administrador
                    SupabaseConfig.client.postgrest["notificacoes"].insert(notificacaoParaAdmin)
                }

                Toast.makeText(requireContext(), "Solicitação cancelada com sucesso!", Toast.LENGTH_SHORT).show()
                // Recarrega a listagem
                carregarStatusUnificado()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao cancelar solicitação", Toast.LENGTH_SHORT).show()
            }
        }
    }
}