package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Molde de espelhamento serializável que bate 100% com as colunas atuais da sua tabela 'livros'
@Serializable
data class MidiaLivroDetalhes(
    val id: String? = null,
    val titulo: String? = null,
    val autor: String? = null,
    val isbn: String? = null,
    @SerialName("capaUrl") val capaUrl: String? = null,
    val audiobook_url: String? = null,
    val pdf_url: String? = null, // Incluído dinamicamente conforme sua nova coluna
    val exemplares: Int? = null,
    val paginas: Int? = null,
    val editora: String? = null,
    val sinopse: String? = null
)

class TelaRF37EditarMidia : Fragment(R.layout.telarf37_editar_midia) {

    private var livroId: String? = null
    private var isMidiaExterna: Boolean = false

    // Componentes de Exibição de Texto
    private lateinit var textTitulo: TextView
    private lateinit var textAutor: TextView
    private lateinit var textSobre: TextView
    private lateinit var textEditora: TextView
    private lateinit var textIsbn13: TextView
    private lateinit var textPaginas: TextView
    private lateinit var textExemplares: TextView
    private lateinit var textAudiobook: TextView
    private lateinit var textPdf: TextView
    private lateinit var textCapaUrl: TextView
    private lateinit var imageLivroDetalhes: ImageView
    private lateinit var buttonApagarMidia: MaterialButton

    // Layouts de clique para Edição (Containers do XML com efeito de clique)
    private lateinit var btnEditarTitulo: View
    private lateinit var btnEditarAutor: View
    private lateinit var btnEditarSobre: View
    private lateinit var btnEditarEditora: View
    private lateinit var btnEditarIsbn: View
    private lateinit var btnEditarPaginas: View
    private lateinit var btnEditarExemplares: View
    private lateinit var btnEditarAudiobook: View
    private lateinit var btnEditarPdf: View
    private lateinit var btnEditarCapaUrl: View

    // Ícones dos Lápis pequenininhos para controle de visibilidade
    private lateinit var iconLapisTitulo: ImageView
    private lateinit var iconLapisAutor: ImageView
    private lateinit var iconLapisSobre: ImageView
    private lateinit var iconLapisEditora: ImageView
    private lateinit var iconLapisIsbn: ImageView
    private lateinit var iconLapisPaginas: ImageView
    private lateinit var iconLapisExemplares: ImageView
    private lateinit var iconLapisAudiobook: ImageView
    private lateinit var iconLapisPdf: ImageView
    private lateinit var iconLapisCapa: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        livroId = arguments?.getString("LIVRO_ID")

        // Vinculando Elementos Visuais de Texto
        textTitulo = view.findViewById(R.id.textTituloLivro)
        textAutor = view.findViewById(R.id.textAutorLivro)
        textSobre = view.findViewById(R.id.textSobreLivro)
        textEditora = view.findViewById(R.id.textEditoraLivro)
        textIsbn13 = view.findViewById(R.id.textIsbn13Livro)
        textPaginas = view.findViewById(R.id.textPaginasLivro)
        textExemplares = view.findViewById(R.id.textExemplaresLivro)
        textAudiobook = view.findViewById(R.id.textAudiobookLivro)
        textPdf = view.findViewById(R.id.textPdfLivro)
        textCapaUrl = view.findViewById(R.id.textCapaUrlLivro)
        imageLivroDetalhes = view.findViewById(R.id.imageLivroDetalhes)
        buttonApagarMidia = view.findViewById(R.id.buttonApagarMidia)

        // Vinculando Áreas de Clique dos Grupos Lineares
        btnEditarTitulo = view.findViewById(R.id.btnEditarTitulo)
        btnEditarAutor = view.findViewById(R.id.btnEditarAutor)
        btnEditarSobre = view.findViewById(R.id.btnEditarSobre)
        btnEditarEditora = view.findViewById(R.id.btnEditarEditora)
        btnEditarIsbn = view.findViewById(R.id.btnEditarIsbn)
        btnEditarPaginas = view.findViewById(R.id.btnEditarPaginas)
        btnEditarExemplares = view.findViewById(R.id.btnEditarExemplares)
        btnEditarAudiobook = view.findViewById(R.id.btnEditarAudiobook)
        btnEditarPdf = view.findViewById(R.id.btnEditarPdf)
        btnEditarCapaUrl = view.findViewById(R.id.btnEditarCapaUrl)

        // Vinculando Lápis discretos
        iconLapisTitulo = view.findViewById(R.id.iconLapisTitulo)
        iconLapisAutor = view.findViewById(R.id.iconLapisAutor)
        iconLapisSobre = view.findViewById(R.id.iconLapisSobre)
        iconLapisEditora = view.findViewById(R.id.iconLapisEditora)
        iconLapisIsbn = view.findViewById(R.id.iconLapisIsbn)
        iconLapisPaginas = view.findViewById(R.id.iconLapisPaginas)
        iconLapisExemplares = view.findViewById(R.id.iconLapisExemplares)
        iconLapisAudiobook = view.findViewById(R.id.iconLapisAudiobook)
        iconLapisPdf = view.findViewById(R.id.iconLapisPdf)
        iconLapisCapa = view.findViewById(R.id.iconLapisCapa)

        if (!livroId.isNullOrEmpty()) {
            carregarDetalhesPeloId(livroId!!)
        } else {
            Toast.makeText(requireContext(), "Erro: ID inválido.", Toast.LENGTH_SHORT).show()
            textTitulo.text = "Erro ao carregar"
        }

        buttonApagarMidia.setOnClickListener { confirmarExclusaoComPopup() }
        configurarCliquesDeEdicao()
    }

    private fun carregarDetalhesPeloId(idDoLivro: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Realiza o select mapeando diretamente para a nossa nova data class estruturada
                val listaDeLivros = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("livros")
                        .select { filter { eq("id", idDoLivro) } }
                        .decodeList<MidiaLivroDetalhes>()
                }

                val livro = listaDeLivros.firstOrNull()

                if (livro != null) {
                    isMidiaExterna = false
                    alternarVisibilidadeDosLapis(visivel = true)

                    // Alimenta os campos de texto respeitando os nomes mapeados do banco
                    textTitulo.text = livro.titulo ?: "--"
                    textAutor.text = livro.autor ?: "--"
                    textSobre.text = livro.sinopse ?: "Sem sinopse cadastrada."
                    textEditora.text = livro.editora ?: "--"
                    textIsbn13.text = livro.isbn ?: "--"
                    textPaginas.text = livro.paginas?.toString() ?: "0"
                    textExemplares.text = livro.exemplares?.toString() ?: "0"
                    textAudiobook.text = livro.audiobook_url ?: "--"
                    textPdf.text = livro.pdf_url ?: "--"
                    textCapaUrl.text = livro.capaUrl ?: "--"

                    // 🚀 Padronizado: Sempre carrega com Coil para aplicar o placeholder se a URL for vazia
                    imageLivroDetalhes.load(livro.capaUrl) {
                        crossfade(true)
                        placeholder(R.drawable.placeholder)
                        error(R.drawable.placeholder)
                    }
                } else {
                    isMidiaExterna = true
                    alternarVisibilidadeDosLapis(visivel = false)
                    configurarComoApenasLeitura()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro de sincronização com o banco.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configurarCliquesDeEdicao() {
        btnEditarTitulo.setOnClickListener { abrirPopupInputReal("titulo", "Título", textTitulo.text.toString(), false, false) }
        btnEditarAutor.setOnClickListener { abrirPopupInputReal("autor", "Autor", textAutor.text.toString(), false, false) }
        btnEditarSobre.setOnClickListener { abrirPopupInputReal("sinopse", "Sobre / Sinopse", textSobre.text.toString(), false, true) }
        btnEditarEditora.setOnClickListener { abrirPopupInputReal("editora", "Editora", textEditora.text.toString(), false, false) }
        btnEditarIsbn.setOnClickListener { abrirPopupInputReal("isbn", "ISBN-13", textIsbn13.text.toString(), false, false) }
        btnEditarPaginas.setOnClickListener { abrirPopupInputReal("paginas", "Páginas", textPaginas.text.toString(), true, false) }
        btnEditarExemplares.setOnClickListener { abrirPopupInputReal("exemplares", "Exemplares", textExemplares.text.toString(), true, false) }
        btnEditarAudiobook.setOnClickListener { abrirPopupInputReal("audiobook_url", "Audiobook URL", textAudiobook.text.toString(), false, false) }
        btnEditarPdf.setOnClickListener { abrirPopupInputReal("pdf_url", "PDF URL", textPdf.text.toString(), false, false) }
        btnEditarCapaUrl.setOnClickListener { abrirPopupInputReal("capaUrl", "Link da Capa", textCapaUrl.text.toString(), false, false) }
    }

    private fun atualizarCampoNoSupabase(coluna: String, novoValor: Any) {
        if (livroId == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("livros").update({
                        set(coluna, novoValor)
                    }) {
                        filter {
                            eq("id", livroId!!)
                        }
                    }
                }
                Toast.makeText(requireContext(), "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                carregarDetalhesPeloId(livroId!!)
            } catch (e: Exception) {
                e.printStackTrace()
                // Mostra o erro real no console para sabermos se o banco rejeitou por outro motivo (ex: tipo de dado)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Erro ao salvar no banco: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun abrirPopupInputReal(colunaBanco: String, labelCampo: String, valorAtual: String, apenasNumeros: Boolean, isMultiLine: Boolean) {
        if (isMidiaExterna) return
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Modificar $labelCampo")

        val input = EditText(requireContext())
        input.setText(if (valorAtual == "--") "" else valorAtual)

        if (apenasNumeros) {
            input.inputType = InputType.TYPE_CLASS_NUMBER
        } else if (isMultiLine) {
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            input.isSingleLine = false
            input.setLines(4)
        } else {
            input.inputType = InputType.TYPE_CLASS_TEXT
        }

        input.setSelection(input.text.length)

        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(50, 25, 50, 25)
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Confirmar") { dialog, _ ->
            val textoInserido = input.text.toString().trim()
            if (textoInserido.isNotEmpty()) {
                // 🌟 Chamando a função correta dependendo do tipo do dado esperado
                if (apenasNumeros) {
                    atualizarCampoInteiroNoSupabase(colunaBanco, textoInserido.toIntOrNull() ?: 0)
                } else {
                    atualizarCampoTextoNoSupabase(colunaBanco, textoInserido)
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // 🌟 FUNÇÃO 1: Dedicada a atualizar colunas do tipo TEXT no Supabase
    private fun atualizarCampoTextoNoSupabase(coluna: String, novoValor: String) {
        if (livroId == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("livros").update({
                        set(coluna, novoValor)
                    }) { filter { eq("id", livroId!!) } }
                }
                Toast.makeText(requireContext(), "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                carregarDetalhesPeloId(livroId!!)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao salvar texto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🌟 FUNÇÃO 2: Dedicada a atualizar colunas do tipo INT4 no Supabase
    private fun atualizarCampoInteiroNoSupabase(coluna: String, novoValor: Int) {
        if (livroId == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("livros").update({
                        set(coluna, novoValor)
                    }) { filter { eq("id", livroId!!) } }
                }
                Toast.makeText(requireContext(), "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                carregarDetalhesPeloId(livroId!!)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao salvar número.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun alternarVisibilidadeDosLapis(visivel: Boolean) {
        val visibilidade = if (visivel) View.VISIBLE else View.GONE
        iconLapisTitulo.visibility = visibilidade
        iconLapisAutor.visibility = visibilidade
        iconLapisSobre.visibility = visibilidade
        iconLapisEditora.visibility = visibilidade
        iconLapisIsbn.visibility = visibilidade
        iconLapisPaginas.visibility = visibilidade
        iconLapisExemplares.visibility = visibilidade
        iconLapisAudiobook.visibility = visibilidade
        iconLapisPdf.visibility = visibilidade
        iconLapisCapa.visibility = visibilidade
    }

    private fun configurarComoApenasLeitura() {
        textTitulo.text = "Mídia Protegida (API)"
        textAutor.text = "Google Books"
        textSobre.text = "Dados protegidos. Mídias importadas via API externa não aceitam modificações locais no banco."
        buttonApagarMidia.isEnabled = false
        buttonApagarMidia.text = "Mídia de Apenas Leitura"
        buttonApagarMidia.setBackgroundColor(android.graphics.Color.GRAY)
    }

    private fun confirmarExclusaoComPopup() {
        val viewPopup = LayoutInflater.from(requireContext()).inflate(R.layout.popup_apagar_conta, null)
        val editSenha = viewPopup.findViewById<EditText>(R.id.editSenhaPopup)
        val iconOlho = viewPopup.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)
        val textErro = viewPopup.findViewById<TextView>(R.id.textErroSenhaPopup)
        val btnConfirmar = viewPopup.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)
        val textTituloPopup = viewPopup.findViewById<TextView>(R.id.textTituloApagarConta)

        textTituloPopup.text = "APAGAR MÍDIA?"

        val builder = AlertDialog.Builder(requireContext()).setView(viewPopup)
        val dialog = builder.create()

        var senhaVisivel = false
        iconOlho.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editSenha.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            editSenha.setSelection(editSenha.text.length)
        }

        btnConfirmar.setOnClickListener {
            val senhaDigitada = editSenha.text.toString().trim()
            val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val senhaAtualDoAdm = sharedPref.getString("USER_SENHA", null)

            if (!senhaAtualDoAdm.isNullOrEmpty() && senhaDigitada == senhaAtualDoAdm) {
                textErro.visibility = View.GONE
                dialog.dismiss()
                apagarLivroDoBanco()
            } else {
                textErro.text = "Senha incorreta. Tente novamente."
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
                    SupabaseConfig.client.from("livros").delete { filter { eq("id", livroId!!) } }
                }
                Toast.makeText(requireContext(), "Mídia apagada com sucesso!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao deletar mídia.", Toast.LENGTH_SHORT).show()
                buttonApagarMidia.isEnabled = true
                buttonApagarMidia.text = "Apagar Mídia"
            }
        }
    }
}