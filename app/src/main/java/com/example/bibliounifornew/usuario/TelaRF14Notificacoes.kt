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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerNotificacoes = view.findViewById(R.id.recyclerNotificacoes)
        textNomeNotif = view.findViewById(R.id.textNomeNotif)
        imagePerfilNotif = view.findViewById(R.id.imagePerfilNotif)

        recyclerNotificacoes.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val emailLogado = sharedPref.getString("USER_EMAIL", "") ?: ""
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

        carregarNotificacoes(emailLogado)
    }

    private fun carregarNotificacoes(email: String) {
        if (email.isEmpty()) return

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

                recyclerNotificacoes.adapter = NotificacaoAdapter(notificacoesDoBanco) { notifClicada ->
                    marcarComoLidaNoBanco(notifClicada, email)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar notificações", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun marcarComoLidaNoBanco(notificacao: Notificacao, email: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 🚀 Agora em vez de deletar, atualizamos o status para "visualizada = true"
                    SupabaseConfig.client.postgrest["notificacoes"]
                        .update(
                            update = {
                                set("visualizada", true)
                            }
                        ) {
                            filter {
                                eq("id", notificacao.id ?: 0)
                            }
                        }
                }
                Toast.makeText(requireContext(), "Notificação marcada como lida!", Toast.LENGTH_SHORT).show()
                carregarNotificacoes(email) 
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao atualizar notificação", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
