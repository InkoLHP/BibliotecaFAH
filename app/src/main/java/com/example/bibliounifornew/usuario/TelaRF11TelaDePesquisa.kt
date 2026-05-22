package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Livro
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TelaRF11TelaDePesquisa : Fragment(R.layout.telarf11_tela_pesquisa) {

    private lateinit var recyclerLivros: RecyclerView
    private lateinit var editPesquisarLivro: EditText
    private lateinit var buttonProcurar: MaterialButton
    private lateinit var iconFiltro: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MAPEAMENTO
        recyclerLivros = view.findViewById(R.id.recyclerLivros)
        editPesquisarLivro = view.findViewById(R.id.editPesquisarLivro)
        buttonProcurar = view.findViewById(R.id.buttonProcurar)
        iconFiltro = view.findViewById(R.id.iconFiltro) // NOVO: Ícone de filtro no XML

        recyclerLivros.layoutManager = LinearLayoutManager(requireContext())

        // RF11.6 - Adaptador inicial vazio
        recyclerLivros.adapter = LivroUsuarioAdapter(emptyList()) { livroClicado ->
            abrirOpcoesLivro(livroClicado) // RF11.7 - Redireciona para as opções (detalhes, desejo, livraria)
        }

        // RF11.5 - Botão procurar
        buttonProcurar.setOnClickListener {
            val pesquisa = editPesquisarLivro.text.toString()

            // Mock de livros simulando a busca
            val livrosFake = listOf(
                Livro(
                    id = "1",
                    titulo = "O Senhor dos Anéis",
                    autor = "J. R. R. Tolkien",
                    isbn = "8595084750",
                    sinopse = "A jornada de Frodo para destruir o Um Anel.",
                    capaResourceId = 0
                ),
                Livro(
                    id = "2",
                    titulo = "Dom Casmurro",
                    autor = "Machado de Assis",
                    isbn = "9786559212466",
                    sinopse = "A clássica história de Bentinho, Capitu e a dúvida do ciúme.",
                    capaResourceId = 0
                )
            )

            recyclerLivros.adapter = LivroUsuarioAdapter(livrosFake) { livroClicado ->
                abrirOpcoesLivro(livroClicado)
            }
        }

        // ABRIR POP-UP DE FILTROS
        iconFiltro.setOnClickListener {
            exibirPopupFiltros()
        }
    }

    // RF11.2, RF11.3, RF11.4 - Exibe o pop-up (BottomSheet) com os filtros
    private fun exibirPopupFiltros() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val viewFiltro = layoutInflater.inflate(R.layout.popup_filtro_pesquisa, null)
        bottomSheet.setContentView(viewFiltro)

        val btnSalvar = viewFiltro.findViewById<MaterialButton>(R.id.buttonSalvarFiltro)
        val btnLimpar = viewFiltro.findViewById<MaterialButton>(R.id.buttonLimparFiltro)

        btnSalvar.setOnClickListener {
            Toast.makeText(requireContext(), "Filtros aplicados!", Toast.LENGTH_SHORT).show()
            bottomSheet.dismiss()
        }

        btnLimpar.setOnClickListener {
            Toast.makeText(requireContext(), "Filtros limpos!", Toast.LENGTH_SHORT).show()
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    // RF11.7 - Simula o clique nos três pontos permitindo escolher a ação
    private fun abrirOpcoesLivro(livro: Livro) {
        val opcoes = arrayOf("Ver Detalhes", "Adicionar à Minha Livraria", "Adicionar à Lista de Desejos")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(livro.titulo)
            .setItems(opcoes) { _, qual ->
                when (qual) {
                    0 -> { // Ver Detalhes
                        val intent = Intent(requireContext(), TelaRF12TelaDoLivro::class.java)
                        intent.putExtra("LIVRO_ID", livro.id)
                        startActivity(intent)
                    }
                    1 -> Toast.makeText(requireContext(), "Adicionado à Livraria!", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext(), "Adicionado à Lista de Desejos!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}