package com.example.bibliounifornew.adm

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.UsuarioAdapter
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*
import com.example.bibliounifornew.data.SupabaseConfig

class Telarf29GerenciamentoUsuariosADM : Fragment(R.layout.telarf29_gerenciamento_usuarios_adm) {

    private lateinit var recyclerUsuarios: RecyclerView
    private lateinit var editPesquisaUsuario: EditText

    // Listas para gerenciar a pesquisa local em tempo real
    private var todosOsUsuarios = mutableListOf<User>()
    private var listaFiltrada = mutableListOf<User>()
    private lateinit var usuarioAdapter: UsuarioAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerUsuarios = view.findViewById(R.id.recyclerUsuarios)
        editPesquisaUsuario = view.findViewById(R.id.editPesquisaUsuario)

        recyclerUsuarios.layoutManager = LinearLayoutManager(requireContext())

        // Configura o adapter inicialmente vazio para não quebrar a tela enquanto o banco carrega
        configurarAdapter()

        // Ativa o ouvinte de digitação na barra de pesquisa
        configurarBarraPesquisa()

        // Configuração segura do botão nativo Voltar
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

        carregarUsuariosDoBanco()
    }

    private fun configurarAdapter() {
        usuarioAdapter = UsuarioAdapter(listaFiltrada) { usuarioSelecionado ->
            // Redireciona para o perfil detalhado do usuário selecionado (Telarf30UsuariosADM)
            val fragmentDestino = Telarf30UsuariosADM().apply {
                arguments = Bundle().apply {
                    putString("nome", usuarioSelecionado.nome)
                    putString("email", usuarioSelecionado.email)
                    putString("foto", usuarioSelecionado.foto)
                    putString("senhaCorreta", usuarioSelecionado.senha)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragmentDestino)
                .addToBackStack(null)
                .commit()
        }
        recyclerUsuarios.adapter = usuarioAdapter
    }

    private fun carregarUsuariosDoBanco() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val usuarios = withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .from("users")
                        .select()
                        .decodeList<User>()
                }

                // 🌟 CORREÇÃO DEFINITIVA: Filtrando usando o campo 'tipo' do seu Model User!
                // Ele remove quem for "adm" (tanto maiúsculo quanto minúsculo) e mantém só os leitores.
                val apenasLeitores = usuarios.filter {
                    it.tipo?.lowercase() != "adm"
                }

                todosOsUsuarios.clear()
                todosOsUsuarios.addAll(apenasLeitores)

                // Inicializa a tela exibindo os leitores na lista
                aplicarFiltroPesquisa()

            } catch (e: Exception) {
                e.printStackTrace()
                val mensagemReal = e.message ?: "Erro desconhecido"
                Toast.makeText(requireContext(), "Erro DB: $mensagemReal", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configurarBarraPesquisa() {
        editPesquisaUsuario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltroPesquisa()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun aplicarFiltroPesquisa() {
        val textoDigitado = editPesquisaUsuario.text.toString().lowercase().trim()

        val resultado = if (textoDigitado.isEmpty()) {
            todosOsUsuarios
        } else {
            todosOsUsuarios.filter { usuario ->
                val nomeUsuario = (usuario.nome ?: "").lowercase()
                val emailUsuario = (usuario.email ?: "").lowercase()

                nomeUsuario.contains(textoDigitado) || emailUsuario.contains(textoDigitado)
            }
        }

        // Atualiza a lista que o adapter está olhando e redesenha a tela
        listaFiltrada.clear()
        listaFiltrada.addAll(resultado)
        usuarioAdapter.notifyDataSetChanged()
    }
}