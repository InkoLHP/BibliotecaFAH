package com.example.bibliounifornew.adm

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
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
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*

class Telarf36AlugueisADM : Fragment(R.layout.telarf36_alugueis_adm) {

    private lateinit var recyclerAlugueis: RecyclerView
    private lateinit var adapter: AluguelADMAdapter
    private var listaMista = mutableListOf<Aluguel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val urlFoto = sharedPref.getString("USER_FOTO", null)

        val imageFotoPerfil = view.findViewById<ImageView>(R.id.imageFotoPerfilAlugueis)
        if (!urlFoto.isNullOrEmpty()) {
            imageFotoPerfil.load(urlFoto) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        recyclerAlugueis = view.findViewById(R.id.recyclerAlugueis)
        recyclerAlugueis.layoutManager = LinearLayoutManager(requireContext())

        adapter = AluguelADMAdapter(
            listaAlugueis = listaMista,
            onVerLivroClick = { aluguel ->
                val fragment = TelaRF37EditarMidia().apply {
                    arguments = Bundle().apply {
                        putString("LIVRO_TITULO", aluguel.titulo_livro)
                        // 🌟 NOVO: Passando autor e capa caso o livro seja do Google Books
                        putString("LIVRO_AUTOR", aluguel.autor_livro)
                        putString("LIVRO_CAPA", aluguel.capa_url)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onVerUsuarioClick = { aluguel ->
                val fragment = Telarf30UsuarioAlugadosADM().apply {
                    arguments = Bundle().apply {
                        putString("email", aluguel.email_usuario)
                        putString("nome", "Estudante")
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
        recyclerAlugueis.adapter = adapter

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

        // Chama a nova função mista
        buscarAlugueisEReservas()
    }

    private fun buscarAlugueisEReservas() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Busca os Aluguéis normais
                val alugueisDoBanco = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"]
                        .select { filter { eq("devolvido", false) } }
                        .decodeList<Aluguel>()
                }

                // 2. Busca as Reservas usando o mesmo modelo (Aluguel)
                val reservasDoBanco = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["reservas"]
                        .select { filter { eq("devolvido", false) } }
                        .decodeList<Aluguel>()
                }

                // (Opcional) Informa ao aplicativo que esses itens vieram da tabela de reservas
                reservasDoBanco.forEach { reserva ->
                    reserva.tagTabela = "reservas"
                }

                listaMista.clear()

                // 3. Junta as duas listas em uma só
                listaMista.addAll(alugueisDoBanco)
                listaMista.addAll(reservasDoBanco)

                // Organiza por ordem alfabética do título do livro
                listaMista.sortBy { it.titulo_livro }

                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao buscar dados do Supabase", Toast.LENGTH_SHORT).show()
            }
        }
    }
}