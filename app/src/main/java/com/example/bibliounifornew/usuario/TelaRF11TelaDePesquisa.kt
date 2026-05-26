package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import com.example.bibliounifornew.model.Livro

class TelaRF11TelaDePesquisa : Fragment(R.layout.telarf11_tela_pesquisa) {

    private lateinit var recyclerLivros: RecyclerView
    private lateinit var editPesquisarLivro: EditText
    private lateinit var buttonProcurar: MaterialButton
    private lateinit var iconFiltro: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerLivros = view.findViewById(R.id.recyclerLivros)
        editPesquisarLivro = view.findViewById(R.id.editPesquisarLivro)
        buttonProcurar = view.findViewById(R.id.buttonProcurar)
        iconFiltro = view.findViewById(R.id.iconFiltro)

        recyclerLivros.layoutManager = LinearLayoutManager(requireContext())
        recyclerLivros.adapter = LivroUsuarioAdapter(emptyList<Livro>()) { livro -> abrirOpcoesLivro(livro) }

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
                val response = com.example.bibliounifornew.api.RetrofitClient.api.searchBooks(query = pesquisa)
                val livrosEncontrados = response.items?.map { item ->
                    val info = item.volumeInfo
                    val isbn13 = info.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier
                    val isbn10 = info.industryIdentifiers?.find { it.type == "ISBN_10" }?.identifier
                    val isbnFinal = isbn13 ?: isbn10 ?: "Sem ISBN"

                    Livro(
                        id = item.id,
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

                recyclerLivros.adapter = LivroUsuarioAdapter(livrosEncontrados) { livro -> abrirOpcoesLivro(livro) }

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

    private fun exibirPopupFiltros() {}

    // 👇 NOVO: Atualiza a foto se ela existir nessa tela
    override fun onResume() {
        super.onResume()
        val sharedPref = requireActivity().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
        val fotoUsuarioUri = sharedPref.getString("USER_FOTO", null)

        // CORRIGIDO PARA O ID DO SEU XML
        val profileImage = view?.findViewById<ImageView>(R.id.imagePerfilBusca)

        if (profileImage != null && !fotoUsuarioUri.isNullOrBlank()) {
            Glide.with(this)
                .load(fotoUsuarioUri)
                .circleCrop()
                .into(profileImage)
        }
    }
}