package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.Adapter.UsuarioAdapter
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*
import com.example.bibliounifornew.data.SupabaseConfig

class Telarf29GerenciamentoUsuariosADM : Fragment(R.layout.telarf29_gerenciamento_usuarios_adm) {

    private lateinit var recyclerUsuarios: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerUsuarios = view.findViewById(R.id.recyclerUsuarios)
        recyclerUsuarios.layoutManager = LinearLayoutManager(requireContext())

        // Configuração segura do botão nativo Voltar
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    parentFragmentManager.popBackStack()
                }
            }
        })

        carregarUsuariosDoBanco()
    }

    private fun carregarUsuariosDoBanco() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Busca os dados na thread de IO para não travar a interface
                val usuarios = withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .from("users") // ✅ CORRIGIDO: Nome exato da tabela no Supabase
                        .select()
                        .decodeList<User>()
                }

                recyclerUsuarios.adapter = UsuarioAdapter(usuarios) { usuarioSelecionado ->
                    // Envia os dados encapsulados para a Tela 30
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

            } catch (e: Exception) {
                e.printStackTrace()
                // Pega a mensagem real do erro que o Supabase/Kotlin retornou
                val mensagemReal = e.message ?: "Erro desconhecido"
                Toast.makeText(requireContext(), "Erro DB: $mensagemReal", Toast.LENGTH_LONG).show()
            }
        }
    }
}