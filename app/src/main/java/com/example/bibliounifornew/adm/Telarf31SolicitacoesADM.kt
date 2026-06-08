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

        // Recuperando a foto do ADM salva na sessão (SharedPreferences)
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val urlFoto = sharedPref.getString("USER_FOTO", null)

        // Mapeando a ImageView e carregando com o Coil
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
            lista = listaInternaSolicitacoes,

            // Lógica do clique implementada com busca no Supabase (Sem Toasts novos)
            onVerUsuarioClick = { solicitacao ->
                if (solicitacao.email_usuario != null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            // Busca o nome e foto do usuário na tabela 'users' pelo e-mail
                            val usuarioEncontrado = withContext(Dispatchers.IO) {
                                SupabaseConfig.client.from("users")
                                    .select {
                                        filter {
                                            eq("email", solicitacao.email_usuario!!)
                                        }
                                    }.decodeSingleOrNull<User>()
                            }

                            // Abre o fragmento com os dados reais encontrados
                            val fragment = Telarf30UsuarioAlugadosADM().apply {
                                arguments = Bundle().apply {
                                    putString("email", solicitacao.email_usuario)
                                    putString("nome", usuarioEncontrado?.nome ?: "Usuário Desconhecido")
                                    putString("foto", usuarioEncontrado?.foto)
                                    putBoolean("apenasAtrasos", false)
                                }
                            }

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.frameLayout, fragment)
                                .addToBackStack(null)
                                .commit()

                        } catch (e: Exception) {
                            e.printStackTrace()
                            // FALLBACK SILENCIOSO: Se houver erro de conexão, abre a tela
                            // apenas com o e-mail, sem disparar nenhum Toast novo na interface.
                            val fragmentFallback = Telarf30UsuarioAlugadosADM().apply {
                                arguments = Bundle().apply {
                                    putString("email", solicitacao.email_usuario)
                                    putString("nome", "Usuário")
                                    putBoolean("apenasAtrasos", false)
                                }
                            }
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.frameLayout, fragmentFallback)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
            },

            // Seu código original de conclusão
            onConcluirSolicitacao = { solicitacao, posicao ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client
                                .from("solicitacoes")
                                .update(update = { set("status", "CONCLUIDA") }) {
                                    filter { eq("id", solicitacao.id!!) }
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

                            SupabaseConfig.client.from("notificacoes").insert(notificacao)
                        }

                        listaInternaSolicitacoes.removeAt(posicao)
                        solicitacaoAdapter.notifyItemRemoved(posicao)
                        Toast.makeText(requireContext(), "Solicitação concluída!", Toast.LENGTH_SHORT).show()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )

        // 👇 ESSAS FORAM AS LINHAS QUE FALTARAM NO SEU CÓDIGO 👇
        recyclerSolicitacoes?.adapter = solicitacaoAdapter

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
                // 1. Busca as solicitações pendentes
                val listaDoBanco = withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .from("solicitacoes")
                        .select {
                            filter { eq("status", "PENDENTE") }
                        }
                        .decodeList<Solicitacao>()
                }

                // 2. Extrai uma lista única com todos os e-mails dessas solicitações
                val listaDeEmails = listaDoBanco.mapNotNull { it.email_usuario }.distinct()
                val mapaDeFotos = mutableMapOf<String, String>()

                // 3. Se houver e-mails, busca as fotos lá na tabela 'users'
                if (listaDeEmails.isNotEmpty()) {
                    val usuarios = withContext(Dispatchers.IO) {
                        SupabaseConfig.client.from("users")
                            .select {
                                filter { isIn("email", listaDeEmails) }
                            }.decodeList<User>()
                    }

                    // Preenche nosso dicionário combinando [E-mail -> Foto]
                    // Preenche nosso dicionário combinando [E-mail -> Foto]
                    usuarios.forEach { user ->
                        // 🌟 CRIAMOS DUAS VARIÁVEIS LOCAIS (val) PARA O KOTLIN CONFIAR
                        val emailUsuario = user.email
                        val fotoUsuario = user.foto

                        if (emailUsuario.isNotEmpty() && !fotoUsuario.isNullOrEmpty()) {
                            mapaDeFotos[emailUsuario] = fotoUsuario
                        }
                    }
                }

                // 4. Atualiza tudo na tela
                listaInternaSolicitacoes.clear()
                listaInternaSolicitacoes.addAll(listaDoBanco)

                // Envia as fotos para o Adapter e atualiza a lista
                solicitacaoAdapter.atualizarFotos(mapaDeFotos)

            } catch (e: Exception) {
                Log.e("SOLICITACOES_ADM", "Erro ao carregar dados: ${e.message}")
                Toast.makeText(requireContext(), "Erro ao conectar com o servidor.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}