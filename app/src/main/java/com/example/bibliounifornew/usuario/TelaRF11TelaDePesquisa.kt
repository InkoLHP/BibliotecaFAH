package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    // 🛡️ Variável de controle para evitar múltiplos cliques simultâneos
    private var processandoClique: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🌟 AJUSTADO: Usando o arquivo original do seu app "user_session"
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailUsuario = sharedPref.getString("USER_EMAIL", "") ?: ""

        // MAPEAMENTO (RF11.1)
        recyclerLivros = view.findViewById(R.id.recyclerLivros)
        editPesquisarLivro = view.findViewById(R.id.editPesquisarLivro)
        buttonProcurar = view.findViewById(R.id.buttonProcurar)
        iconFiltro = view.findViewById(R.id.iconFiltro)

        val profileImage = view.findViewById<ImageView>(R.id.imagePerfilBusca)
        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)

        if (!fotoSalvaUrl.isNullOrEmpty() && profileImage != null) {
            try {
                profileImage.setImageURI(Uri.parse(fotoSalvaUrl))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        recyclerLivros.layoutManager = LinearLayoutManager(requireContext())

        // Inicializa o adapter vazio mapeando as funções reais desde o início
        recyclerLivros.adapter = LivroUsuarioAdapter(
            livros = emptyList(),
            onVerMaisClick = { livro -> abrirOpcoesLivro(livro) },
            onAddListaDesejosClick = { livro -> adicionarAListaDesejos(livro) },
            onAddMinhaLivrariaClick = { livro -> adicionarAMinhaLivraria(livro) }
        )

        // RF11.5 - Botão Procurar fazendo a busca real na Internet
        buttonProcurar.setOnClickListener {
            val pesquisa = editPesquisarLivro.text.toString().trim()

            if (pesquisa.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Digite um título ou autor",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            buscarLivros(pesquisa)
        }

        // RF11.2, RF11.3, RF11.4 - Abre o Pop-up de Filtros Avançados
        iconFiltro.setOnClickListener {
            exibirPopupFiltros()
        }
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

                    val isbn13 = info.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier
                    val isbn10 = info.industryIdentifiers?.find { it.type == "ISBN_10" }?.identifier
                    val isbnFinal = isbn13 ?: isbn10 ?: "Sem ISBN"

                    val idInteiro = item.id.hashCode()

                    Livro(
                        id = idInteiro,
                        titulo = info.title ?: "Sem título",
                        autor = info.authors?.joinToString(", ") ?: "Autor desconhecido",
                        isbn = isbnFinal,
                        capaUrl = info.imageLinks?.thumbnail?.replace("http://", "https://") ?: "",
                        sinopse = info.description,
                        data_publicacao = info.publishedDate,
                        categoria = info.categories?.firstOrNull(),
                        formato = "Físico",
                        disponivel = (0..1).random() == 1,
                        pdfUrl = info.previewLink?.replace("http://", "https://")
                    )
                } ?: emptyList()

                if (livrosEncontrados.isEmpty()) {
                    Toast.makeText(requireContext(), "Nenhum livro encontrado.", Toast.LENGTH_SHORT).show()
                }

                recyclerLivros.adapter = LivroUsuarioAdapter(
                    livros = livrosEncontrados,
                    onVerMaisClick = { livro -> abrirOpcoesLivro(livro) },
                    onAddListaDesejosClick = { livro -> adicionarAListaDesejos(livro) },
                    onAddMinhaLivrariaClick = { livro -> adicionarAMinhaLivraria(livro) }
                )

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    Toast.makeText(requireContext(), "Muitas buscas seguidas. Tente em alguns segundos.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Erro no servidor: ${e.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro de conexão. Verifique sua internet.", Toast.LENGTH_LONG).show()
            } finally {
                buttonProcurar.isEnabled = true
                buttonProcurar.text = "Procurar"
            }
        }
    }

    private fun abrirOpcoesLivro(livro: Livro) {
        val intent = Intent(requireContext(), TelaRF12TelaDoLivro::class.java)
        intent.putExtra("livro", livro)
        startActivity(intent)
    }

    private fun exibirPopupFiltros() {
        // Implementação futura dos filtros
    }

    // 🌟 INSERÇÃO NA TABELA MINHA LIVRARIA (PROTEGIDA)
    private fun adicionarAMinhaLivraria(livro: Livro) {
        if (emailUsuario.isEmpty()) {
            Toast.makeText(requireContext(), "Faça login para adicionar livros", Toast.LENGTH_SHORT).show()
            return
        }

        // Se já houver um processo rodando, ignora totalmente o clique extra
        if (processandoClique) return
        processandoClique = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val item = LivrariaItem(
                    id = null,
                    email_usuario = emailUsuario,
                    livro_id = livro.id,
                    titulo = livro.titulo ?: "Sem título",
                    autor = livro.autor ?: "Autor desconhecido",
                    capa_url = livro.capaUrl,
                    categoria = livro.categoria
                )

                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["minha_livraria"].insert(item)
                }
                Toast.makeText(requireContext(), "Adicionado à Minha Livraria! 📚", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao adicionar à livraria", Toast.LENGTH_SHORT).show()
            } finally {
                // 🔓 Libera o controle para novos cliques após finalizar o processo
                processandoClique = false
            }
        }
    }

    // 🌟 INSERÇÃO NA TABELA LISTA DE DESEJOS (PROTEGIDA)
    private fun adicionarAListaDesejos(livro: Livro) {
        if (emailUsuario.isEmpty()) {
            Toast.makeText(requireContext(), "Faça login para salvar livros", Toast.LENGTH_SHORT).show()
            return
        }

        // Se já houver um processo rodando, ignora totalmente o clique extra
        if (processandoClique) return
        processandoClique = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val item = DesejoItem(
                    id = null,
                    email_usuario = emailUsuario,
                    livro_id = livro.id,
                    titulo = livro.titulo ?: "Sem título",
                    autor = livro.autor ?: "Autor desconhecido",
                    capa_url = livro.capaUrl,
                    categoria = livro.categoria,
                    disponivel = true
                )

                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["lista_desejos"].insert(item)
                }

                val prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                prefs.edit().putString("status_${livro.id}", "NAO_LIDO").apply()

                Toast.makeText(requireContext(), "Salvo na sua Lista de Desejos! ⏱️", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao salvar nos desejos", Toast.LENGTH_SHORT).show()
            } finally {
                // 🔓 Libera o controle para novos cliques após finalizar o processo
                processandoClique = false
            }
        }
    }
}