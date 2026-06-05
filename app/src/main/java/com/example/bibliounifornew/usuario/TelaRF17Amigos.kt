package com.example.bibliounifornew.usuario

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.UsuarioBuscaAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.UsuarioItem
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF17Amigos : Fragment(R.layout.telarf17_amigos) {

    private lateinit var recyclerUsuarios: RecyclerView
    private lateinit var editBuscarAmigo: EditText
    private lateinit var buttonBuscarAmigo: MaterialButton
    private lateinit var imageUsuarioLogado: ImageView

    // 🌟 Variável para guardar o e-mail de quem está usando o app
    private var meuEmailLogado: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerUsuarios = view.findViewById(R.id.recyclerBuscaUsuarios)
        editBuscarAmigo = view.findViewById(R.id.editBuscarAmigo)
        buttonBuscarAmigo = view.findViewById(R.id.buttonBuscarAmigo)
        imageUsuarioLogado = view.findViewById(R.id.imageUsuario)

        recyclerUsuarios.layoutManager = LinearLayoutManager(requireContext())

        // 🌟 Carrega a sessão do SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)

        // 🌟 Captura o e-mail logado (Veja se a chave no seu Login se chama "USER_EMAIL" ou apenas "EMAIL")
        meuEmailLogado = sharedPref.getString("USER_EMAIL", "") ?: ""

        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)
        if (!fotoSalvaUrl.isNullOrEmpty()) {
            imageUsuarioLogado.load(fotoSalvaUrl) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        // Carrega todos os usuários (menos você e menos os ADMs)
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
                val listaUsuarios = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("users").select {
                        filter {
                            // 1. Remove os administradores
                            neq("tipo", "adm")

                            // 2. 🌟 REMOVE SEU PRÓPRIO PERFIL: Se houver um e-mail na sessão, filtra ele fora
                            if (meuEmailLogado.isNotEmpty()) {
                                neq("email", meuEmailLogado)
                            }

                            // 3. Filtro de busca por nome (se digitado)
                            if (pesquisa.isNotEmpty()) {
                                ilike("nome", "%$pesquisa%")
                            }
                        }
                    }.decodeList<UsuarioItem>()
                }

                recyclerUsuarios.adapter = UsuarioBuscaAdapter(
                    listaUsuarios,
                    onCardClick = { usuario -> abrirPerfilAmigo(usuario) },
                    onAdicionarClick = { usuario -> abrirPerfilAmigo(usuario) }
                )

            } catch (e: Exception) {
                e.printStackTrace()
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Erro detalhado do Supabase")
                    .setMessage(e.localizedMessage ?: e.toString())
                    .setPositiveButton("Ok", null)
                    .show()
            } finally {
                buttonBuscarAmigo.isEnabled = true
                buttonBuscarAmigo.text = "Procurar"
            }
        }

        // 📝 Mapeia e insere o Nome do Usuário dinamicamente abaixo da foto
        val textNome = viewContainer.findViewById<TextView>(R.id.textUsuarioAmigos)
        textNome?.text = nomeUsuario
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
}