package com.example.bibliounifornew.usuario

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.UsuarioBuscaAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Amigo
import com.example.bibliounifornew.model.Notificacao // 🌟 Importado o modelo de Notificacao
import com.example.bibliounifornew.model.UsuarioItem
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest // 🌟 Importado para permitir o insert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF17Amigos : Fragment(R.layout.telarf17_amigos) {

    private lateinit var recyclerUsuarios: RecyclerView
    private lateinit var editBuscarAmigo: EditText
    private lateinit var buttonBuscarAmigo: MaterialButton
    private lateinit var imageUsuarioLogado: ImageView

    // Variável para guardar o e-mail de quem está usando o app
    private var meuEmailLogado: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerUsuarios = view.findViewById(R.id.recyclerBuscaUsuarios)
        editBuscarAmigo = view.findViewById(R.id.editBuscarAmigo)
        buttonBuscarAmigo = view.findViewById(R.id.buttonBuscarAmigo)
        imageUsuarioLogado = view.findViewById(R.id.imageUsuarioAmigos)

        recyclerUsuarios.layoutManager = LinearLayoutManager(requireContext())

        // Carrega a sessão do SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)

        // Captura dados do usuário logado
        meuEmailLogado = sharedPref.getString("USER_EMAIL", "") ?: ""
        val meuNomeLogado = sharedPref.getString("USER_NOME", "Usuário")

        // Exibe o nome do usuário no título do cabeçalho
        view.findViewById<TextView>(R.id.textTituloBuscaAmigo)?.text = "Olá, $meuNomeLogado"

        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)
        if (!fotoSalvaUrl.isNullOrEmpty()) {
            imageUsuarioLogado.load(fotoSalvaUrl) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        // Carrega todos os usuários inicialmente
        buscarUsuariosNoBanco("")

        buttonBuscarAmigo.setOnClickListener {
            val termo = editBuscarAmigo.text.toString().trim()
            buscarUsuariosNoBanco(termo)
        }
    }

    private fun buscarUsuariosNoBanco(pesquisa: String) {
        buttonBuscarAmigo.isEnabled = false
        buttonBuscarAmigo.text = "Buscando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 🌟 1. Primeiro, baixa a SUA lista de amigos
                val meusAmigos = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("amigos").select {
                        filter {
                            eq("usuario_email", meuEmailLogado)
                        }
                    }.decodeList<Amigo>()
                }

                // Extrai apenas os e-mails para facilitar
                val listaDeEmailsAmigos = meusAmigos.map { it.amigo_email }

                // 2. Depois, baixa a lista de todos os usuários (como já estava)
                val listaUsuarios = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("users").select {
                        filter {
                            neq("tipo", "adm")
                            if (meuEmailLogado.isNotEmpty()) { neq("email", meuEmailLogado) }
                            if (pesquisa.isNotEmpty()) { ilike("nome", "%$pesquisa%") }
                        }
                    }.decodeList<UsuarioItem>()
                }

                // 🌟 3. Passa a lista de e-mails de amigos para o Adapter
                recyclerUsuarios.adapter = UsuarioBuscaAdapter(
                    listaUsuarios,
                    listaDeEmailsAmigos, // <-- Passando a lista aqui!
                    onCardClick = { usuario -> abrirPerfilAmigo(usuario) },
                    onAdicionarClick = { usuario ->
                        val email = usuario.email
                        val nome = usuario.nome
                        if (!email.isNullOrEmpty() && !nome.isNullOrEmpty()) {
                            enviarConviteAmizade(email, nome)
                        } else {
                            Toast.makeText(requireContext(), "Dados do usuário incompletos.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao buscar usuários", Toast.LENGTH_SHORT).show()
            } finally {
                buttonBuscarAmigo.isEnabled = true
                buttonBuscarAmigo.text = "Procurar"
            }
        }
    }

    private fun abrirPerfilAmigo(usuario: UsuarioItem) {
        val fragmentPerfil = TelaRF17PerfilAmigo().apply {
            arguments = Bundle().apply {
                putString("AMIGO_ID", usuario.id?.toString())
                putString("AMIGO_NOME", usuario.nome)
                putString("AMIGO_EMAIL", usuario.email)
                putString("AMIGO_FOTO", usuario.foto)
                putString("AMIGO_BIO", usuario.bio)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragmentPerfil)
            .addToBackStack(null)
            .commit()
    }

    // 🌟 Função para registrar a notificação de amizade no banco de dados
    private fun enviarConviteAmizade(destinatarioEmail: String, destinatarioNome: String) {
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val meuNome = sharedPref.getString("USER_NOME", "Alguém") ?: "Alguém"
        val meuEmail = sharedPref.getString("USER_EMAIL", "") ?: ""

        if (meuEmail.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Prepara a notificação usando o modelo atualizado
                    val novoConvite = Notificacao(
                        email_usuario = destinatarioEmail,
                        titulo = "Novo Pedido de Amizade \uD83D\uDC4B",
                        mensagem = "$meuNome enviou uma solicitação de amizade para você.",
                        visualizada = false,
                        tipo = "convite_amizade",
                        remetente_email = meuEmail
                    )

                    // Insere no banco na tabela de notificações
                    SupabaseConfig.client.postgrest["notificacoes"].insert(novoConvite)
                }
                Toast.makeText(requireContext(), "Convite enviado para $destinatarioNome!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                // 🌟 Mudamos aqui para mostrar o erro real na tela!
                Toast.makeText(requireContext(), "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}