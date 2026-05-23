package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Livro
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class TelaRF11TelaDePesquisa : Fragment(R.layout.telarf11_tela_pesquisa) {

    private lateinit var recyclerLivros: RecyclerView
    private lateinit var editPesquisarLivro: EditText
    private lateinit var buttonProcurar: MaterialButton
    private lateinit var iconFiltro: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MAPEAMENTO (RF11.1)
        recyclerLivros = view.findViewById(R.id.recyclerLivros)
        editPesquisarLivro = view.findViewById(R.id.editPesquisarLivro)
        buttonProcurar = view.findViewById(R.id.buttonProcurar)
        iconFiltro = view.findViewById(R.id.iconFiltro)

        recyclerLivros.layoutManager = LinearLayoutManager(requireContext())

        // Inicializa o adapter vazio
        recyclerLivros.adapter = LivroUsuarioAdapter(emptyList()) { livroClicado ->
            abrirOpcoesLivro(livroClicado)
        }

        // RF11.5 - Botão Procurar fazendo a busca real na Internet
        buttonProcurar.setOnClickListener {
            val pesquisa = editPesquisarLivro.text.toString().trim()

            if (pesquisa.isEmpty()) {
                Toast.makeText(requireContext(), "Digite um título ou autor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chamando a internet usando Coroutine
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Busca dados direto do banco online do Supabase
                    val livrosEncontrados = SupabaseConfig.client.postgrest["livros"].select {
                        filter {
                            or {
                                ilike("titulo", "%$pesquisa%")
                                ilike("autor", "%$pesquisa%")
                            }
                        }
                    }.decodeList<Livro>()

                    if (livrosEncontrados.isEmpty()) {
                        Toast.makeText(requireContext(), "Nenhum livro correspondente encontrado.", Toast.LENGTH_SHORT).show()
                        recyclerLivros.adapter = LivroUsuarioAdapter(emptyList()) { abrirOpcoesLivro(it) }
                    } else {
                        // RF11.6 - Renderiza os cards reais com Imagem, Nome, Autor e Data
                        recyclerLivros.adapter = LivroUsuarioAdapter(livrosEncontrados) { livroClicado ->
                            abrirOpcoesLivro(livroClicado)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // RF11.2, RF11.3, RF11.4 - Abre o Pop-up de Filtros Avançados
        iconFiltro.setOnClickListener {
            exibirPopupFiltros()
        }
    }

    // RF11.2, RF11.3, RF11.4 - Exibe o BottomSheet de Filtros
    private fun exibirPopupFiltros() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val viewFiltro = layoutInflater.inflate(R.layout.popup_filtro_pesquisa, null)
        bottomSheet.setContentView(viewFiltro)

        val btnSalvar = viewFiltro.findViewById<MaterialButton>(R.id.buttonSalvarFiltro)
        val btnLimpar = viewFiltro.findViewById<MaterialButton>(R.id.buttonLimparFiltro)

        // TODO: Mapear os campos internos do seu R.layout.popup_filtro_pesquisa
        // como os Checkboxes/Spinners de Categoria, Online/Presencial (RF11.3) e Disponibilidade (RF11.4)

        btnSalvar.setOnClickListener {
            Toast.makeText(requireContext(), "Filtros aplicados!", Toast.LENGTH_SHORT).show()
            bottomSheet.dismiss()
            // Aqui futuramente você pode aplicar a lógica para filtrar a lista vinda do Supabase
        }

        btnLimpar.setOnClickListener {
            Toast.makeText(requireContext(), "Filtros limpos!", Toast.LENGTH_SHORT).show()
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    // RF11.7 - Menu de opções acionado ao clicar nos três pontos
    private fun abrirOpcoesLivro(livro: Livro) {
        val statusDisponibilidade = if (livro.disponível) "Disponível" else "Já Alugado" // RF11.4
        val opcoes = arrayOf("Ver Detalhes ($statusDisponibilidade)", "Adicionar à Minha Livraria", "Adicionar à Lista de Desejos")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(livro.titulo)
            .setItems(opcoes) { _, qual ->
                when (qual) {
                    0 -> { // Ver Detalhes
                        val intent = Intent(requireContext(), TelaRF12TelaDoLivro::class.java)
                        intent.putExtra("LIVRO_ID", livro.id)
                        startActivity(intent)
                    }
                    1 -> Toast.makeText(requireContext(), "${livro.titulo} adicionado à Livraria!", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext(), "${livro.titulo} adicionado aos Desejos!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}