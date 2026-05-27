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
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.AluguelAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.Notificacao
import com.example.bibliounifornew.model.Solicitacao
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRF13StatusAluguel : Fragment(R.layout.telarf13_status) {

    private lateinit var recyclerAlugueis: RecyclerView
    private lateinit var textNenhumLivro: TextView

    private var emailUsuario: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerAlugueis = view.findViewById(R.id.recyclerAlugueis)
        textNenhumLivro = view.findViewById(R.id.textNenhumLivro)

        recyclerAlugueis.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireActivity().getSharedPreferences(
            "user_session",
            Context.MODE_PRIVATE
        )

        emailUsuario = sharedPref.getString("USER_EMAIL", "")
            ?.trim()
            ?.lowercase()
            ?: ""

        carregarStatusUnificado()
    }

    private fun carregarStatusUnificado() {
        if (emailUsuario.isEmpty()) {
            textNenhumLivro.visibility = View.VISIBLE
            textNenhumLivro.text = "Usuário não identificado."
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val listaCompleta = withContext(Dispatchers.IO) {

                    val buscaAlugueis = async {
                        try {
                            SupabaseConfig.client
                                .postgrest["alugueis"]
                                .select {
                                    filter {
                                        eq("email_usuario", emailUsuario)
                                    }
                                }
                                .decodeList<Aluguel>()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList<Aluguel>()
                        }
                    }

                    val buscaSolicitacoes = async {
                        try {
                            SupabaseConfig.client
                                .postgrest["solicitacoes"]
                                .select {
                                    filter {
                                        eq("email_usuario", emailUsuario)
                                    }
                                }
                                .decodeList<Solicitacao>()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList<Solicitacao>()
                        }
                    }

                    val alugueisAtivos = buscaAlugueis.await()
                    val solicitacoesAbertas = buscaSolicitacoes.await()

                    val solicitacoesConvertidas = solicitacoesAbertas.map { sol ->
                        Aluguel(
                            id = sol.id ?: 0L,
                            email_usuario = sol.email_usuario,
                            titulo_livro = sol.titulo,
                            autor_livro = sol.autor,
                            capa_url = sol.capa_url,
                            data_vencimento = "Status: ${sol.status}",
                            dias_restantes = if (sol.tipo_solicitacao == "PDF_DIGITAL") 1L else 2L,
                            devolvido = false,
                            tipo = "SOLICITACAO"
                        )
                    }

                    (alugueisAtivos + solicitacoesConvertidas)
                        .sortedByDescending { it.id ?: 0L }
                }

                if (listaCompleta.isEmpty()) {
                    textNenhumLivro.visibility = View.VISIBLE
                    textNenhumLivro.text = "Nenhum aluguel ou solicitação encontrada."
                    recyclerAlugueis.visibility = View.GONE
                } else {
                    textNenhumLivro.visibility = View.GONE
                    recyclerAlugueis.visibility = View.VISIBLE

                    recyclerAlugueis.adapter = AluguelAdapter(listaCompleta) { itemClicado, ehSolicitacao ->
                        val idSeguro = itemClicado.id ?: 0L
                        val tituloSeguro = itemClicado.titulo_livro ?: "Livro sem título"

                        if (ehSolicitacao || itemClicado.tipo == "SOLICITACAO") {
                            cancelarSolicitacaoReal(idSeguro, tituloSeguro)
                        } else if (itemClicado.tipo == "RESERVA") {
                            cancelarReservaReal(itemClicado)
                        } else {
                            cancelarAluguelReal(itemClicado, emailUsuario)
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                textNenhumLivro.visibility = View.VISIBLE
                textNenhumLivro.text = "Erro ao carregar dados."
            }
        }
    }

    private fun cancelarAluguelReal(aluguel: Aluguel, emailUsuarioLogado: String) {
        val tituloSeguro = aluguel.titulo_livro ?: "Livro"
        val dataHoraAtual = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())

        val notificacaoParaAdmin = Notificacao(
            email_usuario = "admin@biblioteca.com",
            titulo = "Aluguel Cancelado",
            mensagem = "O usuário '$emailUsuarioLogado' cancelou o aluguel do livro '$tituloSeguro'.",
            visualizada = false,
            created_at = dataHoraAtual
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .postgrest["alugueis"]
                        .delete {
                            filter {
                                eq("id", aluguel.id ?: 0L)
                            }
                        }

                    SupabaseConfig.client
                        .postgrest["notificacoes"]
                        .insert(notificacaoParaAdmin)
                }

                Toast.makeText(requireContext(), "Aluguel cancelado!", Toast.LENGTH_LONG).show()
                carregarStatusUnificado()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao cancelar aluguel.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelarSolicitacaoReal(idSolicitacao: Long, tituloLivro: String) {
        val dataHoraAtual = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())

        val notificacaoParaAdmin = Notificacao(
            email_usuario = "admin@biblioteca.com",
            titulo = "Solicitação Cancelada",
            mensagem = "O usuário '$emailUsuario' cancelou a solicitação do livro '$tituloLivro'.",
            visualizada = false,
            created_at = dataHoraAtual
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .postgrest["solicitacoes"]
                        .delete {
                            filter {
                                eq("id", idSolicitacao)
                            }
                        }

                    SupabaseConfig.client
                        .postgrest["notificacoes"]
                        .insert(notificacaoParaAdmin)
                }

                Toast.makeText(requireContext(), "Solicitação cancelada!", Toast.LENGTH_SHORT).show()
                carregarStatusUnificado()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao cancelar solicitação.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelarReservaReal(reserva: Aluguel) {
        val tituloSeguro = reserva.titulo_livro ?: "Livro"
        val dataHoraAtual = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())

        val notificacao = Notificacao(
            email_usuario = emailUsuario,
            titulo = "Reserva Cancelada",
            mensagem = "Sua reserva do livro '$tituloSeguro' foi cancelada.",
            visualizada = false,
            created_at = dataHoraAtual
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .postgrest["alugueis"]
                        .delete {
                            filter {
                                eq("id", reserva.id ?: 0L)
                            }
                        }

                    SupabaseConfig.client
                        .postgrest["notificacoes"]
                        .insert(notificacao)
                }

                Toast.makeText(requireContext(), "Reserva cancelada!", Toast.LENGTH_SHORT).show()
                carregarStatusUnificado()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao cancelar reserva.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}