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
import com.example.bibliounifornew.model.Livro
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

        nomeUsuario = arguments?.getString("nome")
        emailUsuario = arguments?.getString("email")
        fotoUsuario = arguments?.getString("foto")

        val textNome = view.findViewById<TextView>(R.id.textNomeUsuario)
        val textEmail = view.findViewById<TextView>(R.id.textEmailUsuario)
        val imagemFoto = view.findViewById<ImageView>(R.id.imageFotoUsuarioDetalhe)

        textNome.text = nomeUsuario ?: "Usuário Desconhecido"
        textEmail.text = emailUsuario ?: "Sem e-mail"

        if (!fotoUsuario.isNullOrEmpty()) {
            imagemFoto.load(fotoUsuario) {
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        recyclerAlugados = view.findViewById(R.id.recyclerAlugados)
        recyclerAlugados.layoutManager = LinearLayoutManager(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })

        if (emailUsuario != null) {
            carregarAlugueisDoUsuario(emailUsuario!!)
        }
    }

    private fun carregarAlugueisDoUsuario(email: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val lista = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("alugueis").select {
                        filter { eq("email_usuario", email) }
                    }.decodeList<Aluguel>()
                }

                recyclerAlugados.adapter = AluguelADMAdapter(
                    listaAlugueis = lista,
                    onVerLivroClick = { aluguel ->
                        val livro = Livro(
                            id = aluguel.id_livro ?: "",
                            titulo = aluguel.titulo_livro ?: "",
                            autor = aluguel.autor_livro ?: "",
                            isbn = "",
                            capaUrl = aluguel.capa_url ?: "",
                            sinopse = "Visualizado via Perfil do Usuário",
                            data_publicacao = null,
                            categoria = null,
                            formato = "Físico",
                            disponivel = false,
                            pdfUrl = null
                        )
                        val telaDetalhes = TelaRF12TelaDoLivroADM().apply {
                            arguments = Bundle().apply { putSerializable("livro", livro) }
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, telaDetalhes)
                            .addToBackStack(null)
                            .commit()
                    },
                    onVerUsuarioClick = {
                        Toast.makeText(requireContext(), "Você já está no perfil deste usuário", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar aluguéis", Toast.LENGTH_SHORT).show()
            }
        }
    }
}