package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.adapter.LivroUsuarioAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import com.example.bibliounifornew.model.Livro
import com.example.bibliounifornew.model.LivrariaItem
import com.example.bibliounifornew.model.DesejoItem
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF11TelaDePesquisa : Fragment(R.layout.telarf11_tela_pesquisa) {

    private lateinit var recyclerLivros: RecyclerView
    private lateinit var editPesquisarLivro: EditText
    private lateinit var buttonProcurar: MaterialButton
    private lateinit var iconFiltro: ImageView
    private var emailUsuario: String = ""
    private var processandoClique: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailUsuario = sharedPref.getString("USER_EMAIL", "") ?: ""

        recyclerLivros = view.findViewById(R.id.recyclerLivros)
        editPesquisarLivro = view.findViewById(R.id.editPesquisarLivro)
        buttonProcurar = view.findViewById(R.id.buttonProcurar)
        iconFiltro = view.findViewById(R.id.iconFiltro)

        // Foto de Perfil
        val profileImage = view.findViewById<ImageView>(R.id.imagePerfilBusca)
        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)
        if (!fotoSalvaUrl.isNullOrEmpty() && profileImage != null) {
            profileImage.load(fotoSalvaUrl) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        recyclerLivros.layoutManager = LinearLayoutManager(requireContext())

        buttonProcurar.setOnClickListener {
            val pesquisa = editPesquisarLivro.text.toString().trim()
            if (pesquisa.isEmpty()) {
                Toast.makeText(requireContext(), "Digite um título ou autor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            buscarLivros(pesquisa)
        }

        iconFiltro.setOnClickListener { exibirPopupFiltros() }
    }

    private fun buscarLivros(pesquisa: String) {
        buttonProcurar.isEnabled = false
        buttonProcurar.text = "Buscando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = com.example.bibliounifornew.api.RetrofitClient
                    .api
                    .searchBooks(query = pesquisa)

                val livrosEncontrados = response.items?.map { item ->
                    val info = item.volumeInfo
                    Livro(
                        id = item.id.hashCode(),
                        titulo = info.title ?: "Sem título",
                        autor = info.authors?.joinToString(", ") ?: "Autor desconhecido",
                        isbn = info.industryIdentifiers?.firstOrNull()?.identifier ?: "Sem ISBN",
                        capaUrl = info.imageLinks?.thumbnail?.replace("http://", "https://") ?: "",
                        sinopse = info.description,
                        data_publicacao = info.publishedDate,
                        categoria = info.categories?.firstOrNull(),
                        formato = "Físico",
                        disponivel = (0..1).random() == 1,
                        pdfUrl = info.previewLink?.replace("http://", "https://")
                    )
                } ?: emptyList()

                recyclerLivros.adapter = LivroUsuarioAdapter(
                    livros = livrosEncontrados,
                    onVerMaisClick = { livro -> abrirOpcoesLivro(livro) },
                    onAddListaDesejosClick = { livro -> adicionarAListaDesejos(livro) },
                    onAddMinhaLivrariaClick = { livro -> adicionarAMinhaLivraria(livro) }
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro na busca", Toast.LENGTH_SHORT).show()
            } finally {
                buttonProcurar.isEnabled = true
                buttonProcurar.text = "Procurar"
            }
        }
    }

    private fun exibirPopupFiltros() {
        val popup = PopupMenu(requireContext(), iconFiltro)

        // --- SUBMENU FICÇÃO (Nomes em PT) ---
        val fics = popup.menu.addSubMenu("Ficção")
        fics.add("Ficção Geral")
        fics.add("Fantasia")
        fics.add("Ficção Científica")
        fics.add("Romance")
        fics.add("Terror")
        fics.add("Aventura")
        fics.add("Distopia")

        // --- SUBMENU NÃO FICÇÃO (Nomes em PT) ---
        val nonFics = popup.menu.addSubMenu("Não Ficção")
        nonFics.add("Biografia")
        nonFics.add("Autoajuda")
        nonFics.add("Negócios e Economia")
        nonFics.add("História")
        nonFics.add("Filosofia")
        nonFics.add("Ciências")
        nonFics.add("Saúde e Bem-estar")
        nonFics.add("Religião e Espiritualidade")

        popup.setOnMenuItemClickListener { item ->
            if (item.hasSubMenu()) return@setOnMenuItemClickListener false

            // MAPEAMENTO: Nome em PT -> Termo em EN para a API
            val categoriaEN = when (item.title.toString()) {
                "Ficção Geral" -> "Fiction"
                "Fantasia" -> "Fantasy"
                "Ficção Científica" -> "Science Fiction"
                "Romance" -> "Romance"
                "Terror" -> "Horror"
                "Aventura" -> "Adventure"
                "Distopia" -> "Dystopian"
                "Biografia" -> "Biography"
                "Autoajuda" -> "Self-Help"
                "Negócios e Economia" -> "Business"
                "História" -> "History"
                "Filosofia" -> "Philosophy"
                "Ciências" -> "Science"
                "Saúde e Bem-estar" -> "Health"
                "Religião e Espiritualidade" -> "Religion"
                else -> item.title.toString()
            }

            val termoBusca = editPesquisarLivro.text.toString().trim()
            if (termoBusca.isEmpty()) {
                buscarLivros("subject:\"$categoriaEN\"")
            } else {
                buscarLivros("$termoBusca+subject:\"$categoriaEN\"")
            }
            true
        }
        popup.show()
    }

    private fun abrirOpcoesLivro(livro: Livro) {
        val intent = Intent(requireContext(), TelaRF12TelaDoLivro::class.java)
        intent.putExtra("livro", livro)
        startActivity(intent)
    }

    private fun adicionarAMinhaLivraria(livro: Livro) {
        if (emailUsuario.isEmpty()) return
        if (processandoClique) return
        processandoClique = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val item = LivrariaItem(null, emailUsuario, livro.id, livro.titulo, livro.autor, livro.capaUrl, livro.categoria)
                withContext(Dispatchers.IO) { SupabaseConfig.client.postgrest["minha_livraria"].insert(item) }
                Toast.makeText(requireContext(), "Adicionado! 📚", Toast.LENGTH_SHORT).show()
            } finally { processandoClique = false }
        }
    }

    private fun adicionarAListaDesejos(livro: Livro) {
        if (emailUsuario.isEmpty()) return
        if (processandoClique) return
        processandoClique = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val item = DesejoItem(null, emailUsuario, livro.id, livro.titulo, livro.autor, livro.capaUrl, livro.categoria, true)
                withContext(Dispatchers.IO) { SupabaseConfig.client.postgrest["lista_desejos"].insert(item) }
                val prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                prefs.edit().putString("status_${livro.id}", "NAO_LIDO").apply()
                Toast.makeText(requireContext(), "Salvo! ⏱️", Toast.LENGTH_SHORT).show()
            } finally { processandoClique = false }
        }
    }
}