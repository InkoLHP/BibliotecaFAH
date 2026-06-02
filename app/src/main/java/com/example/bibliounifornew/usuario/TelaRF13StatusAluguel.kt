package com.example.bibliounifornew.usuario

import android.content.Context // ✅ Adicionado import para o SharedPref
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.AluguelUSERAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.Solicitacao
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF13StatusAluguel : Fragment(R.layout.telarf13_status) {

    private lateinit var recyclerStatus: RecyclerView
    private val listaMistaExibicao = mutableListOf<Aluguel>()
    private lateinit var userAdapter: AluguelUSERAdapter

    // ✅ CORRIGIDO: Removido o e-mail fake "usuario@unifor.br"
    private var emailUsuarioLogado: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ CORRIGIDO: Recupera dinamicamente a sessão real do usuário logado
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailUsuarioLogado = sharedPref.getString("USER_EMAIL", "") ?: ""

        recyclerStatus = view.findViewById(R.id.recyclerAlugueis)
        recyclerStatus.layoutManager = LinearLayoutManager(requireContext())

        userAdapter = AluguelUSERAdapter(listaMistaExibicao) { itemSelecionado, ehSolicitacao ->
            if (ehSolicitacao) {
                cancelarSolicitacaoNoBanco(itemSelecionado)
            } else {
                cancelarAluguelNoBanco(itemSelecionado)
            }
        }
        recyclerStatus.adapter = userAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, TelaRF08DashboardUsuario())
                        .commit()
                }
            }
        })

        // Só chama o banco se o e-mail não estiver vazio
        if (emailUsuarioLogado.isNotEmpty()) {
            carregarDadosDoUsuario()
        } else {
            Toast.makeText(requireContext(), "Erro: Sessão do usuário não encontrada.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarDadosDoUsuario() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val alugueisAtivos = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"]
                        .select { filter { eq("email_usuario", emailUsuarioLogado); eq("devolvido", false) } }
                        .decodeList<Aluguel>()
                }

                val solicitacoesPendentes = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["solicitacoes"]
                        .select { filter { eq("email_usuario", emailUsuarioLogado) } }
                        .decodeList<Solicitacao>()
                }

                val solicitacoesConvertidas = solicitacoesPendentes.map { solicitacao ->
                    Aluguel(
                        id = solicitacao.id,
                        email_usuario = solicitacao.email_usuario,
                        titulo_livro = solicitacao.titulo.ifEmpty { "Sem título" },
                        autor_livro = solicitacao.autor.ifEmpty { "Autor desconhecido" },
                        capa_url = solicitacao.capa_url,
                        data_vencimento = "Status: ${solicitacao.status.ifEmpty { "PENDENTE" }}",
                        dias_restantes = if (solicitacao.tipo_solicitacao == "PDF") 1L else 0L,
                        devolvido = false
                    )
                }

                listaMistaExibicao.clear()
                listaMistaExibicao.addAll(alugueisAtivos)
                listaMistaExibicao.addAll(solicitacoesConvertidas)
                userAdapter.notifyDataSetChanged()

                // 💡 Dica extra para debugar: avisa se o banco do usuário logado realmente não tiver nada
                if (listaMistaExibicao.isEmpty()) {
                    Toast.makeText(requireContext(), "Você não possui nenhuma reserva ou aluguel ativo.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao atualizar status: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cancelarSolicitacaoNoBanco(aluguel: Aluguel) {
        val idSeguro = aluguel.id ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["solicitacoes"].delete { filter { eq("id", idSeguro) } }
                }
                Toast.makeText(requireContext(), "Solicitação cancelada com sucesso!", Toast.LENGTH_SHORT).show()
                carregarDadosDoUsuario()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao cancelar solicitação", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelarAluguelNoBanco(aluguel: Aluguel) {
        Toast.makeText(requireContext(), "Contate a administração para devolver este livro.", Toast.LENGTH_SHORT).show()
    }
}