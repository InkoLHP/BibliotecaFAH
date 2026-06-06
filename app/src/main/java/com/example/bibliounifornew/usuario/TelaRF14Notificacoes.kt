package com.example.bibliounifornew.usuario

import android.content.Context
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
import com.example.bibliounifornew.adapter.NotificacaoAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Amigo
import com.example.bibliounifornew.model.Notificacao
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF14Notificacoes : Fragment(R.layout.telarf14_notificacoes) {

    private lateinit var recyclerNotificacoes: RecyclerView
    private lateinit var textNomeNotif: TextView
    private lateinit var imagePerfilNotif: ImageView
    private var emailLogado: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerNotificacoes = view.findViewById(R.id.recyclerNotificacoes)
        textNomeNotif = view.findViewById(R.id.textNomeNotif)
        imagePerfilNotif = view.findViewById(R.id.imagePerfilNotif)

        recyclerNotificacoes.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailLogado = sharedPref.getString("USER_EMAIL", "") ?: ""
        val nomeLogado = sharedPref.getString("USER_NOME", "Usuário") ?: ""
        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)

        textNomeNotif.text = nomeLogado

        if (!fotoSalvaUrl.isNullOrEmpty()) {
            imagePerfilNotif.load(fotoSalvaUrl) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        if (emailLogado.isNotEmpty()) {
            carregarNotificacoes(emailLogado)
        } else {
            Toast.makeText(requireContext(), "Erro: Usuário não identificado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarNotificacoes(email: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val notificacoesDoBanco = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["notificacoes"]
                        .select {
                            filter {
                                eq("email_usuario", email)
                            }
                            order(column = "created_at", order = Order.DESCENDING)
                        }
                        .decodeList<Notificacao>()
                }

                // 🌟 Adapter atualizado com os novos parâmetros
                recyclerNotificacoes.adapter = NotificacaoAdapter(
                    listaNotificacoes = notificacoesDoBanco,
                    onAvisoLido = { notifClicada -> marcarComoLidaNoBanco(notifClicada, email) },
                    onAceitarConvite = { convite -> aceitarAmizade(convite, email) },
                    onRecusarConvite = { convite -> marcarComoLidaNoBanco(convite, email) } // Recusar apenas deleta a notificação
                )

                if (notificacoesDoBanco.isEmpty()) {
                    Toast.makeText(requireContext(), "Sua caixa de notificações está vazia.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar notificações: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun marcarComoLidaNoBanco(notificacao: Notificacao, email: String) {
        val idNotificacao = notificacao.id ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["notificacoes"]
                        .delete {
                            filter {
                                eq("id", idNotificacao)
                            }
                        }
                }
                carregarNotificacoes(email) // Recarrega a lista para sumir da tela

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao excluir notificação", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🌟 Nova função para lidar com o clique no botão "Aceitar"
    private fun aceitarAmizade(notificacao: Notificacao, meuEmail: String) {
        val emailRemetente = notificacao.remetente_email ?: return
        val idNotificacao = notificacao.id ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 🌟 1. Prepara as DUAS vias da amizade
                    val amizadeIda = Amigo(usuario_email = meuEmail, amigo_email = emailRemetente)
                    val amizadeVolta = Amigo(usuario_email = emailRemetente, amigo_email = meuEmail)

                    // 🌟 2. Salva as duas linhas no banco ao mesmo tempo
                    SupabaseConfig.client.postgrest["amigos"].insert(listOf(amizadeIda, amizadeVolta))

                    // 🌟 3. Deleta a notificação
                    SupabaseConfig.client.postgrest["notificacoes"]
                        .delete { filter { eq("id", idNotificacao) } }
                }
                Toast.makeText(requireContext(), "Amizade aceita!", Toast.LENGTH_SHORT).show()
                carregarNotificacoes(meuEmail)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao aceitar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}