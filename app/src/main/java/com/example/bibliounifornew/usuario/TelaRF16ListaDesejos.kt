package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.DesejosAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.DesejoItem
import com.example.bibliounifornew.model.LivrariaItem
import com.example.bibliounifornew.model.Livro
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF16ListaDesejos : Fragment(R.layout.telarf16_lista_desejos) {

    private lateinit var recyclerDesejos: RecyclerView

    // 🎨 Componentes do Header (Informações de Login)
    private lateinit var textEmailDesejos: TextView
    private lateinit var imagePerfilDesejos: ImageView

    private var emailUsuario: String = ""
    private var listaItens: MutableList<DesejoItem> = mutableListOf()
    private lateinit var adapterDesejos: DesejosAdapter

    private var processandoClique: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Referenciar os componentes do XML
        recyclerDesejos = view.findViewById(R.id.recyclerListaDesejos)
        textEmailDesejos = view.findViewById(R.id.txtEmailUsuarioDesejos) // Verifique esse ID no seu XML
        imagePerfilDesejos = view.findViewById(R.id.imageUsuarioDesejos) // Verifique esse ID no seu XML

        recyclerDesejos.layoutManager = LinearLayoutManager(requireContext())

        // 2. Lendo do arquivo unificado "user_session"
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailUsuario = sharedPref.getString("USER_EMAIL", "") ?: ""
        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)

        // 3. Setar as informações no Header
        textEmailDesejos.text = emailUsuario

        if (!fotoSalvaUrl.isNullOrEmpty()) {
            imagePerfilDesejos.load(fotoSalvaUrl) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        // 4. Carregar os dados do banco
        if (emailUsuario.isNotEmpty()) {
            carregarListaDesejos()
        } else {
            Toast.makeText(requireContext(), "Usuário não identificado.", Toast.LENGTH_LONG).show()
        }
    }

    private fun carregarListaDesejos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["lista_desejos"]
                        .select {
                            filter {
                                eq("email_usuario", emailUsuario)
                            }
                        }.decodeList<DesejoItem>()
                }

                listaItens = resultado.toMutableList()

                adapterDesejos = DesejosAdapter(
                    itens = listaItens,
                    onCapaClick = { item -> abrirDetalhesDoLivro(item) },
                    onRemoverClick = { item -> removerDosDesejos(item) },
                    onLivrariaClick = { item -> moverParaLivraria(item) },
                    onAlugarClick = { item -> alugarLivro(item) }
                )
                recyclerDesejos.adapter = adapterDesejos

                if (listaItens.isEmpty()) {
                    Toast.makeText(requireContext(), "Sua lista de desejos está vazia!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar lista", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removerDosDesejos(item: DesejoItem) {
        if (processandoClique) return
        processandoClique = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["lista_desejos"].delete {
                        filter { eq("id", item.id ?: 0) }
                    }
                }
                Toast.makeText(requireContext(), "${item.titulo} removido!", Toast.LENGTH_SHORT).show()
                carregarListaDesejos()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao remover", Toast.LENGTH_SHORT).show()
            } finally {
                processandoClique = false
            }
        }
    }

    private fun moverParaLivraria(item: DesejoItem) {
        if (processandoClique) return
        processandoClique = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val novoItemLivraria = LivrariaItem(
                    id = null,
                    email_usuario = emailUsuario,
                    livro_id = item.livro_id,
                    titulo = item.titulo,
                    autor = item.autor,
                    capa_url = item.capa_url,
                    categoria = item.categoria
                )

                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["minha_livraria"].insert(novoItemLivraria)
                    SupabaseConfig.client.postgrest["lista_desejos"].delete {
                        filter { eq("id", item.id ?: 0) }
                    }
                }

                Toast.makeText(requireContext(), "Movido para a Minha Livraria!", Toast.LENGTH_SHORT).show()
                carregarListaDesejos()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao mover livro", Toast.LENGTH_SHORT).show()
            } finally {
                processandoClique = false
            }
        }
    }

    private fun alugarLivro(item: DesejoItem) {
        if (processandoClique) return
        processandoClique = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Lógica de aluguel futura aqui
                Toast.makeText(requireContext(), "Solicitando aluguel de: ${item.titulo}", Toast.LENGTH_SHORT).show()
            } finally {
                processandoClique = false
            }
        }
    }

    private fun abrirDetalhesDoLivro(item: DesejoItem) {
        val livroMapeado = Livro(
            id = item.livro_id ?: 0,
            titulo = item.titulo,
            autor = item.autor,
            isbn = "Sem ISBN",
            capaUrl = item.capa_url ?: "",
            sinopse = "",
            data_publicacao = null,
            categoria = item.categoria,
            formato = "Físico",
            disponivel = item.disponivel ?: true,
            pdfUrl = null
        )

        val intent = Intent(requireContext(), TelaRF12TelaDoLivro::class.java).apply {
            putExtra("livro", livroMapeado)
        }
        startActivity(intent)
    }
}