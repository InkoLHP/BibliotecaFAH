package com.example.bibliounifornew.adm

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.LivrosAdmAdapter
import com.example.bibliounifornew.api.RetrofitClient
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LivroCadastrado(
    @SerialName("id")
    val id: String,

    @SerialName("titulo")
    val titulo: String? = null,

    @SerialName("autor")
    val autor: String? = null,

    @SerialName("isbn")
    val isbn: String? = null,

    @SerialName("capaUrl")
    val capaUrl: String? = null,

    val veioDaApi: Boolean = false
)

class Telarf32LivrosCrudADM : Fragment(R.layout.telarf32_livros_crud_adm) {

    private lateinit var adapter: LivrosAdmAdapter
    private lateinit var editPesquisaLivro: EditText

    private var todosOsLivrosSupabase = mutableListOf<LivroCadastrado>()
    private var listaFiltradaExibicao = mutableListOf<LivroCadastrado>()

    private var buscaApiJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🌟 CORREÇÃO DE ID: Vinculado ao ID correto definido no seu XML
        editPesquisaLivro = view.findViewById(R.id.etProcurarMidia)

        // Recuperando a foto do ADM salva na sessão
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val urlFoto = sharedPref.getString("USER_FOTO", null)

        val imageFotoPerfil = view.findViewById<ImageView>(R.id.imageFotoPerfilLivrosCrud)
        if (!urlFoto.isNullOrEmpty() && imageFotoPerfil != null) {
            imageFotoPerfil.load(urlFoto) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        // Configuração do botão voltar nativo
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

        // Botão "Adicionar Nova Mídia"
        val btnAdicionarMidia = view.findViewById<MaterialButton>(R.id.btnAdicionarMidia)
        btnAdicionarMidia.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, TelaRF33CadastroDeLivros())
                .addToBackStack(null)
                .commit()
        }

        // Configurar o RecyclerView
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerLivrosAdm)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Como está dentro de um NestedScrollView, isso evita engasgos na rolagem
        recycler.isNestedScrollingEnabled = false

        adapter = LivrosAdmAdapter(listaFiltradaExibicao) { livroClicado ->
            if (livroClicado.veioDaApi) {
                Toast.makeText(requireContext(), "Este livro pertence à API do Google e não pode ser editado.", Toast.LENGTH_SHORT).show()
            } else {
                val argumentos = Bundle().apply {
                    putString("LIVRO_ID", livroClicado.id)
                }
                val telaDetalhes = TelaRF37EditarMidia().apply { arguments = argumentos }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, telaDetalhes)
                    .addToBackStack(null)
                    .commit()
            }
        }
        recycler.adapter = adapter

        configurarBarraPesquisa()
        buscarLivrosDoBanco()
    }

    private fun buscarLivrosDoBanco() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val livrosBuscados = withContext(Dispatchers.IO) {
                    SupabaseConfig.client
                        .from("livros")
                        .select()
                        .decodeList<LivroCadastrado>()
                }

                todosOsLivrosSupabase.clear()
                todosOsLivrosSupabase.addAll(livrosBuscados)

                aplicarFiltroLocalESubsidiarApi()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar acervo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configurarBarraPesquisa() {
        editPesquisaLivro.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltroLocalESubsidiarApi()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        editPesquisaLivro.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val texto = editPesquisaLivro.text.toString().trim()
                if (texto.isNotEmpty()) buscarDadosDaGoogleBooksAPI(texto)
                true
            } else {
                false
            }
        }
    }

    private fun aplicarFiltroLocalESubsidiarApi() {
        val textoDigitado = editPesquisaLivro.text.toString().lowercase().trim()

        val resultadoLocal = if (textoDigitado.isEmpty()) {
            todosOsLivrosSupabase
        } else {
            todosOsLivrosSupabase.filter { livro ->
                val titulo = (livro.titulo ?: "").lowercase()
                val autor = (livro.autor ?: "").lowercase()
                val isbn = (livro.isbn ?: "").lowercase()

                titulo.contains(textoDigitado) || autor.contains(textoDigitado) || isbn.contains(textoDigitado)
            }
        }

        listaFiltradaExibicao.clear()
        listaFiltradaExibicao.addAll(resultadoLocal)
        adapter.notifyDataSetChanged()

        buscaApiJob?.cancel()
        if (textoDigitado.length >= 3) {
            buscaApiJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(800)
                buscarDadosDaGoogleBooksAPI(textoDigitado)
            }
        }
    }

    private fun buscarDadosDaGoogleBooksAPI(termo: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val respostaApi = withContext(Dispatchers.IO) {
                    RetrofitClient.api.searchBooks(termo)
                }

                val livrosConvertidosApi = respostaApi.items?.map { itemApi ->
                    val info = itemApi.volumeInfo

                    val isbnConvertido = info?.industryIdentifiers?.firstOrNull { it.type == "ISBN_13" }?.identifier
                        ?: info?.industryIdentifiers?.firstOrNull()?.identifier
                        ?: ""

                    LivroCadastrado(
                        id = itemApi.id ?: java.util.UUID.randomUUID().toString(),
                        titulo = info?.title ?: "Título indisponível",
                        autor = info?.authors?.joinToString(", ") ?: "Autor Desconhecido",
                        isbn = isbnConvertido,
                        capaUrl = info?.imageLinks?.thumbnail?.replace("http://", "https://"),
                        veioDaApi = true
                    )
                } ?: emptyList()

                if (livrosConvertidosApi.isNotEmpty()) {
                    listaFiltradaExibicao.removeAll { it.veioDaApi }
                    listaFiltradaExibicao.addAll(livrosConvertidosApi)
                    adapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}