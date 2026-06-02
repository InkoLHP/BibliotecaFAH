package com.example.bibliounifornew.adm

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
import com.example.bibliounifornew.adapter.AluguelADMAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Telarf30UsuarioAlugadosADM : Fragment(R.layout.telarf30_usuario_alugados_adm) {

    private lateinit var recyclerAlugados: RecyclerView

    private var emailUsuario: String? = null
    private var nomeUsuario: String? = null
    private var fotoUsuario: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Recebe os dados
        nomeUsuario = arguments?.getString("nome")
        emailUsuario = arguments?.getString("email")
        fotoUsuario = arguments?.getString("foto")

        // 2. Preenche o Header Premium
        val textNome = view.findViewById<TextView>(R.id.textNomeUsuario)
        val textEmail = view.findViewById<TextView>(R.id.textEmailUsuario)

        // Pega a ImageView que está dentro do CardView (já que ela não tem ID no XML)
        val cardFoto = view.findViewById<androidx.cardview.widget.CardView>(R.id.cardFotoUsuario)
        val imagemFoto = cardFoto.getChildAt(0) as ImageView

        textNome.text = nomeUsuario ?: "Usuário Desconhecido"
        textEmail.text = emailUsuario ?: "Sem e-mail"

        if (!fotoUsuario.isNullOrEmpty()) {
            imagemFoto.load(fotoUsuario) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
        }

        // 3. Configura o RecyclerView
        recyclerAlugados = view.findViewById(R.id.recyclerAlugados)
        recyclerAlugados.layoutManager = LinearLayoutManager(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, Telarf29GerenciamentoUsuariosADM())
                        .commit()
                }
            }
        })

        // 4. Inicia a busca
        if (emailUsuario != null) {
            carregarAlugueisDoUsuario(emailUsuario!!)
        } else {
            Toast.makeText(context, "Erro: E-mail não encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarAlugueisDoUsuario(email: String) {
        val apenasAtrasos = arguments?.getBoolean("apenasAtrasos") ?: false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val todosAlugueis = withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .from("alugueis")
                        .select {
                            filter {
                                eq("email_usuario", email)
                            }
                        }
                        .decodeList<Aluguel>()
                }

                val listaExibida = if (apenasAtrasos) {
                    todosAlugueis.filter { it.dias_restantes != null && it.dias_restantes < 0 && it.devolvido == false }
                } else {
                    todosAlugueis
                }

                recyclerAlugados.adapter = AluguelADMAdapter(
                    listaAlugueis = listaExibida,
                    onVerLivroClick = { aluguel ->
                        // Ação ao clicar para ver detalhes/editar mídia
                        val fragment = TelaRF37EditarMidia().apply {
                            arguments = Bundle().apply {
                                putString("LIVRO_TITULO", aluguel.titulo_livro)
                            }
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, fragment)
                            .addToBackStack(null)
                            .commit()
                    },
                    onVerUsuarioClick = { aluguel ->
                        // Já está na tela do usuário, pode ignorar ou exibir um Toast informativo
                    }
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar livros: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}