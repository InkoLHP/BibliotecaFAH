package com.example.bibliounifornew.usuario

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.AluguelUSERAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.put

class TelaRF13StatusAluguel : Fragment(R.layout.telarf13_status) {

    private lateinit var tagTabela: String
    private lateinit var recyclerStatus: RecyclerView
    private lateinit var textNenhumLivro: TextView
    private lateinit var textNomeUsuario: TextView
    private lateinit var imagePerfil: ImageView

    private val listaGeralExibicao = mutableListOf<Aluguel>()
    private lateinit var userAdapter: AluguelUSERAdapter
    private var emailUsuarioLogado: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerStatus = view.findViewById(R.id.recyclerAlugueis)
        textNenhumLivro = view.findViewById(R.id.textNenhumLivro)
        textNomeUsuario = view.findViewById(R.id.textNomeUsuarioAlugados)
        imagePerfil = view.findViewById(R.id.imagePerfilAlugados)

        recyclerStatus.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailUsuarioLogado = sharedPref.getString("USER_EMAIL", "") ?: ""
        val nomeUsuario = sharedPref.getString("USER_NOME", "Usuário") ?: ""
        val fotoUsuarioUrl = sharedPref.getString("USER_FOTO", null)

        textNomeUsuario.text = nomeUsuario
        if (!fotoUsuarioUrl.isNullOrEmpty()) {
            imagePerfil.load(fotoUsuarioUrl) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        userAdapter = AluguelUSERAdapter(listaGeralExibicao, "alugueis") { itemSelecionado, tabelaOrigem ->
            when (tabelaOrigem.lowercase()) {
                "solicitacoes" -> cancelarRegistroNoBanco(itemSelecionado, "solicitacoes", "Solicitação cancelada com sucesso!")
                "reservas" -> cancelarRegistroNoBanco(itemSelecionado, "reservas", "Reserva cancelada com sucesso!")
                "alugueis" -> cancelarRegistroNoBanco(itemSelecionado, "alugueis", "Aluguel encerrado com sucesso!")
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

        if (emailUsuarioLogado.isNotEmpty()) {
            carregarDadosDoUsuario()
        } else {
            Toast.makeText(requireContext(), "Erro: Sessão do usuário não encontrada.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarDadosDoUsuario() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Busca Aluguéis Ativos
                val alugueisAtivos = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"]
                        .select {
                            filter {
                                eq("email_usuario", emailUsuarioLogado)
                                eq("devolvido", false)
                                eq("oculto_historico", false)
                            }
                            order(column = "id", order = Order.DESCENDING)
                        }
                        .decodeList<Aluguel>()
                }.map { it.apply { tagTabela = "alugueis" } }

                // 2. Busca Solicitações Ativas
                val solicitacoesAtivas = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["solicitacoes"]
                        .select {
                            filter {
                                eq("email_usuario", emailUsuarioLogado)
                                eq("status", "PENDENTE")
                            }
                            order(column = "id", order = Order.DESCENDING)
                        }
                        .decodeList<Aluguel>()
                }.map { it.apply { tagTabela = "solicitacoes" } }

                // 3. Busca Reservas Ativas
                val reservasAtivas = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["reservas"]
                        .select {
                            filter {
                                eq("email_usuario", emailUsuarioLogado)
                                eq("oculto_historico", false)
                            }
                            order(column = "id", order = Order.DESCENDING)
                        }
                        .decodeList<Aluguel>()
                }.map { it.apply { tagTabela = "reservas" } }

                // Une as listas mantendo a estrutura de pilha pelo ID
                val listaMisturada = mutableListOf<Aluguel>().apply {
                    addAll(alugueisAtivos)
                    addAll(solicitacoesAtivas)
                    addAll(reservasAtivas)
                }

                listaMisturada.sortByDescending { it.id ?: 0L }

                listaGeralExibicao.clear()
                listaGeralExibicao.addAll(listaMisturada)

                userAdapter.notifyDataSetChanged()

                if (listaGeralExibicao.isEmpty()) {
                    textNenhumLivro.visibility = View.VISIBLE
                    recyclerStatus.visibility = View.GONE
                } else {
                    textNenhumLivro.visibility = View.GONE
                    recyclerStatus.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar dados: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cancelarRegistroNoBanco(item: Aluguel, tabela: String, mensagemSucesso: String) {
        val idSeguro = item.id ?: return

        val tituloLivro = if (!item.titulo_livro.isNullOrBlank()) item.titulo_livro else (item.titulo ?: "Livro")

        val timestampAtual = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {

                    // 1️⃣ Executa a atualização lógica baseada nas colunas reais da tabela
                    val jsonUpdateStatus = kotlinx.serialization.json.buildJsonObject {
                        if (tabela.lowercase() == "solicitacoes") {
                            put("status", "cancelado")
                        } else {
                            put("oculto_historico", true)
                            if (tabela.lowercase() == "alugueis") {
                                put("devolvido", true)
                            }
                        }
                    }

                    SupabaseConfig.client.postgrest[tabela].update(jsonUpdateStatus) {
                        filter { eq("id", idSeguro) }
                    }

                    val termoAcao = when(tabela.lowercase()) {
                        "alugueis" -> "aluguel"
                        "reservas" -> "reserva"
                        else -> "solicitação"
                    }

                    val pronome = when(tabela.lowercase()) {
                        "alugueis" -> "o seu"
                        else -> "a sua"
                    }

                    val statusCancelamento = when(tabela.lowercase()) {
                        "alugueis" -> "Cancelado"
                        else -> "Cancelada"
                    }

                    // Limpa notificações duplicadas antigas desse livro
                    SupabaseConfig.client.postgrest["notificacoes"].delete {
                        filter {
                            eq("email_usuario", emailUsuarioLogado)
                            ilike("mensagem", "%$tituloLivro%")
                        }
                    }

                    // 2️⃣ Envia Notificação para o Usuário
                    val jsonNotifUsuario = kotlinx.serialization.json.buildJsonObject {
                        put("email_usuario", emailUsuarioLogado)

                        put(
                            "titulo",
                            "${termoAcao.replaceFirstChar { it.uppercase() }} $statusCancelamento"
                        )

                        put(
                            "mensagem",
                            "Você cancelou com sucesso $pronome $termoAcao do livro: $tituloLivro."
                        )

                        put("created_at", timestampAtual)
                    }
                    SupabaseConfig.client.postgrest["notificacoes"].insert(jsonNotifUsuario)

                    // 3️⃣ Envia Notificação para o Administrador
                    val jsonNotifAdm = kotlinx.serialization.json.buildJsonObject {
                        put("email_usuario", "adm@unifor.br")
                        put("titulo", "Cancelamento de $termoAcao")
                        put("mensagem", "O usuário $emailUsuarioLogado cancelou o $termoAcao do livro: $tituloLivro.")
                        put("created_at", timestampAtual)
                    }
                    SupabaseConfig.client.postgrest["notificacoes"].insert(jsonNotifAdm)
                }

                Toast.makeText(requireContext(), mensagemSucesso, Toast.LENGTH_SHORT).show()
                carregarDadosDoUsuario()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao cancelar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}