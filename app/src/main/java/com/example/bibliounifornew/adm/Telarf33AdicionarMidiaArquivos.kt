package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 1. O Molde agora bate exatamente com as colunas da sua tabela do Supabase
@Serializable
data class NovoLivro(
    val titulo: String,
    val autor: String,
    val isbn: String,

    @SerialName("capaUrl") // Força o Kotlin a mandar com o 'U' maiúsculo idêntico ao banco
    val capaUrl: String,

    val audiobook_url: String,
    val exemplares: Int,
    val paginas: Int,
    val editora: String,
    val disponivel_braille: Boolean
)

class Telarf33AdicionarMidiaArquivos : Fragment(R.layout.telarf33_adicionar_midia_arquivos) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vinculando os componentes do layout (XML corrigido)
        val editAudiobook = view.findViewById<EditText>(R.id.editArquivoAudiobook)
        val checkBraille = view.findViewById<CheckBox>(R.id.checkBraille)
        val btnSalvarLivro = view.findViewById<MaterialButton>(R.id.btnSalvarLivro)

        // Resgatando os dados vindos das telas anteriores
        val titulo = arguments?.getString("TITULO") ?: ""
        val autor = arguments?.getString("AUTOR") ?: ""
        val isbn = arguments?.getString("ISBN") ?: ""
        val capa = arguments?.getString("CAPA") ?: ""
        val editora = arguments?.getString("EDITORA") ?: ""

        // Conversão segura para Inteiros (conforme o tipo 'integer' do seu banco)
        val exemplares = arguments?.getString("EXEMPLARES")?.toIntOrNull() ?: 0
        val paginas = arguments?.getString("PAGINAS")?.toIntOrNull() ?: 0

        btnSalvarLivro.setOnClickListener {
            btnSalvarLivro.isEnabled = false
            btnSalvarLivro.text = "Salvando..."

            // 2. Montando o objeto purificado apenas com o que o banco aceita
            val livroParaSalvar = NovoLivro(
                titulo = titulo,
                autor = autor,
                isbn = isbn,
                capaUrl = capa,
                audiobook_url = editAudiobook.text.toString().trim(),
                exemplares = exemplares,
                paginas = paginas,
                editora = editora,
                disponivel_braille = checkBraille.isChecked
            )

            // 3. Envio direto para o banco de dados
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        SupabaseConfig.client.postgrest["livros"].insert(livroParaSalvar)
                    }
                    mostrarPopupSucesso()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Erro ao salvar no Supabase: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    btnSalvarLivro.isEnabled = true
                    btnSalvarLivro.text = "Salvar novo livro"
                }
            }
        }
    }

    private fun mostrarPopupSucesso() {
        val viewPopup = LayoutInflater.from(requireContext()).inflate(R.layout.popup_livro_salvo_sucesso, null)
        val btnVoltarMidias = viewPopup.findViewById<MaterialButton>(R.id.btnVoltarMidias)

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        builder.setView(viewPopup)
        builder.setCancelable(false)

        val dialog = builder.create()

        btnVoltarMidias.setOnClickListener {
            dialog.dismiss()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf27CrudADM())
                .commit()
        }
        dialog.show()
    }
}