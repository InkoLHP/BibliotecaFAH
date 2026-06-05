package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
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
import com.example.bibliounifornew.adm.MidiaLivroDetalhes // Reutilizando seu modelo serializável estável
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
            buscarLivrosMesclados(pesquisa)
        }

        iconFiltro.setOnClickListener { exibirPopupFiltros() }
    }

    private fun buscarLivrosMesclados(pesquisa: String) {
        buttonProcurar.isEnabled = false
        buttonProcurar.text = "Buscando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 🚀 BUSCA ASSÍNCRONA PARALELA: Dispara as duas buscas na mesma fração de segundo
                val buscaSupabase = async(Dispatchers.IO) {
                    try {
                        // Filtra se o termo está contido no título OR no autor (ignora case)
                        SupabaseConfig.client.from("livros").select {
                            filter {
                                or {
                                    ilike("titulo", "%$pesquisa%")
                                    ilike("autor", "%$pesquisa%")
                                }
                            }
                        }.decodeList<MidiaLivroDetalhes>()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList<MidiaLivroDetalhes>()
                    }
                }

                val buscaGoogleBooks = async(Dispatchers.IO) {
                    try {
                        com.example.bibliounifornew.api.RetrofitClient.api.searchBooks(query = pesquisa)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                // Aguarda o término de ambas de forma simultânea
                val localResult = buscaSupabase.await()
                val apiResult = buscaGoogleBooks.await()

                // 1. Mapeia os livros internos do Supabase (Prioridade Máxima)
                val livrosSupabase = localResult.map { local ->
                    Livro(
                        id = local.id?.hashCode() ?: (1000..9999).random(),
                        titulo = local.titulo ?: "Sem título",
                        autor = local.autor ?: "Autor desconhecido",
                        isbn = local.isbn ?: "Sem ISBN",
                        capaUrl = local.capaUrl ?: "",
                        sinopse = local.sinopse,
                        data_publicacao = "--",
                        categoria = "Biblioteca Local", // Tag visual identificadora
                        formato = "Físico / Digital",
                        disponivel = (local.exemplares ?: 0) > 0,
                        pdfUrl = local.pdf_url
                    )
                }

                // 2. Mapeia os livros da API Externa do Google
                val livrosGoogle = apiResult?.items?.map { item ->
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

                // 3. MESCLAGEM INTELIGENTE: Remove itens duplicados com base no título e autor
                val listaCompletaFinal = mutableListOf<Livro>()
                listaCompletaFinal.addAll(livrosSupabase)

                for (googleLivro in livrosGoogle) {
                    // Evita inserir o mesmo livro da API caso ele já tenha sido puxado do Supabase
                    val jaExisteNoBanco = livrosSupabase.any {
                        it.titulo.equals(googleLivro.titulo, ignoreCase = true) ||
                                (it.isbn != "Sem ISBN" && it.isbn == googleLivro.isbn)
                    }
                    if (!jaExisteNoBanco) {
                        listaCompletaFinal.add(googleLivro)
                    }
                }

                // Atualiza o adapter com a listagem combinada
                recyclerLivros.adapter = LivroUsuarioAdapter(
                    livros = listaCompletaFinal,
                    onVerMaisClick = { livro -> abrirOpcoesLivro(livro) },
                    onAddListaDesejosClick = { livro -> adicionarAListaDesejos(livro) },
                    onAddMinhaLivrariaClick = { livro -> adicionarAMinhaLivraria(livro) }
                )

                if (listaCompletaFinal.isEmpty()) {
                    Toast.makeText(requireContext(), "Nenhum livro localizado.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao processar busca unificada", Toast.LENGTH_SHORT).show()
            } finally {
                buttonProcurar.isEnabled = true
                buttonProcurar.text = "Procurar"
            }
        }
    }

    private fun exibirPopupFiltros() {
        val popup = PopupMenu(requireContext(), iconFiltro)

        val fics = popup.menu.addSubMenu("Ficção")
        fics.add("Ficção Geral")
        fics.add("Fantasia")
        fics.add("Ficção Científica")
        fics.add("Romance")
        fics.add("Terror")
        fics.add("Aventura")
        fics.add("Distopia")

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
                buscarLivrosMesclados("subject:\"$categoriaEN\"")
            } else {
                buscarLivrosMesclados("$termoBusca+subject:\"$categoriaEN\"")
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