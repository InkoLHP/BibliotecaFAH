package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF37EditarMidia : Fragment(R.layout.telarf37_editar_midia) {

    private var livroId: String? = null

    // Componentes visuais da tela principal
    private lateinit var textTitulo: TextView
    private lateinit var textAutor: TextView
    private lateinit var textSobre: TextView
    private lateinit var textEditora: TextView
    private lateinit var textIsbn13: TextView
    private lateinit var textPaginas: TextView
    private lateinit var imageLivroDetalhes: ImageView
    private lateinit var buttonApagarMidia: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Resgata o ID do livro enviado pela tela anterior
        livroId = arguments?.getString("LIVRO_ID")

        // 2. Inicializa os componentes do XML da tela principal
        textTitulo = view.findViewById(R.id.textTituloLivro)
        textAutor = view.findViewById(R.id.textAutorLivro)
        textSobre = view.findViewById(R.id.textSobreLivro)
        textEditora = view.findViewById(R.id.textEditoraLivro)
        textIsbn13 = view.findViewById(R.id.textIsbn13Livro)
        textPaginas = view.findViewById(R.id.textPaginasLivro)
        imageLivroDetalhes = view.findViewById(R.id.imageLivroDetalhes)
        buttonApagarMidia = view.findViewById(R.id.buttonApagarMidia)

        // 3. Se o ID existir, busca as informações do banco para preencher a tela
        if (livroId != null) {
            carregarDetalhesDoSupabase(livroId!!)
        } else {
            Toast.makeText(requireContext(), "Erro: ID do livro não encontrado.", Toast.LENGTH_SHORT).show()
            textTitulo.text = "Erro ao carregar"
            textSobre.text = "Volte para a tela anterior e tente novamente."
        }

        // 4. Configura o botão vermelho de Apagar
        buttonApagarMidia.setOnClickListener {
            confirmarExclusaoComPopup()
        }
    }

    private fun carregarDetalhesDoSupabase(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val livro = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["livros"]
                        .select {
                            filter { eq("id", id) }
                        }.decodeSingle<NovoLivro>()
                }

                // Preenche os textos
                textTitulo.text = livro.titulo ?: "Título Indisponível"
                textAutor.text = livro.autor ?: "Autor Desconhecido"
                textSobre.text = "Sem descrição disponível."
                textEditora.text = livro.editora ?: "--"
                textIsbn13.text = livro.isbn ?: "--"
                textPaginas.text = livro.paginas.toString()

                // Carrega a capa usando o Glide
                if (!livro.capaUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(livro.capaUrl)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder)
                        .into(imageLivroDetalhes)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar detalhes", Toast.LENGTH_LONG).show()
                textSobre.text = "Erro de conexão com o banco de dados."
            }
        }
    }

    private fun confirmarExclusaoComPopup() {
        // CORRIGIDO: Agora aponta exatamente para o seu arquivo popup_apagar_conta.xml
        val viewPopup = LayoutInflater.from(requireContext()).inflate(R.layout.popup_apagar_conta, null)

        // Vincula os componentes de dentro do POP-UP
        val textTituloPopup = viewPopup.findViewById<TextView>(R.id.textTituloApagarConta)
        val editSenha = viewPopup.findViewById<EditText>(R.id.editSenhaPopup)
        val iconOlho = viewPopup.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)
        val textErro = viewPopup.findViewById<TextView>(R.id.textErroSenhaPopup)
        val btnConfirmar = viewPopup.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)

        // Modifica o título de "APAGAR CONTA?" para "APAGAR MÍDIA?" dinamicamente
        textTituloPopup.text = "APAGAR MÍDIA?"

        // Cria o Dialog do Android
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(viewPopup)
        val dialog = builder.create()

        // Lógica de mostrar/ocultar senha no olho do pop-up
        var senhaVisivel = false
        iconOlho.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editSenha.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            editSenha.setSelection(editSenha.text.length) // Mantém o cursor no final
        }

        // Clique do botão confirmar dentro do Pop-up
        btnConfirmar.setOnClickListener {
            val senhaDigitada = editSenha.text.toString().trim()

            // Altere "admin123" para a senha real que deseja validar!
            if (senhaDigitada == "admin123") {
                textErro.visibility = View.GONE
                dialog.dismiss() // Fecha o pop-up
                apagarLivroDoBanco() // Deleta o livro do Supabase
            } else {
                // Se errar a senha, mostra o aviso vermelho do seu XML
                textErro.visibility = View.VISIBLE
            }
        }

        dialog.show()
    }

    private fun apagarLivroDoBanco() {
        if (livroId == null) return

        buttonApagarMidia.isEnabled = false
        buttonApagarMidia.text = "Apagando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["livros"].delete {
                        filter { eq("id", livroId!!) }
                    }
                }
                Toast.makeText(requireContext(), "Mídia apagada com sucesso!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao apagar: ${e.message}", Toast.LENGTH_LONG).show()
                buttonApagarMidia.isEnabled = true
                buttonApagarMidia.text = "Apagar Mídia"
            }
        }
    }
}