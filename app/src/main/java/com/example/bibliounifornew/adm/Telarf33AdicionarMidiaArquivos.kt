package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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

// Molde purificado: Bate exatamente com as colunas da sua tabela do Supabase
@Serializable
data class NovoLivro(
    val titulo: String,
    val autor: String,
    val isbn: String,
    @SerialName("capaUrl") val capaUrl: String,
    val audiobook_url: String,
    val pdf_url: String,
    val sinopse: String,
    val exemplares: Int,
    val paginas: Int,
    val editora: String
)

class Telarf33AdicionarMidiaArquivos : Fragment(R.layout.telarf33_adicionar_midia_arquivos) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vinculando os componentes do XML Premium atualizado
        val editAudiobook = view.findViewById<EditText>(R.id.editArquivoAudiobook)
        val editPdf = view.findViewById<EditText>(R.id.editArquivoPdf)
        val btnSalvarLivro = view.findViewById<MaterialButton>(R.id.btnSalvarLivro)

        // 🌟 CORREÇÃO: Resgatando os dados com as chaves minúsculas vindas da Tela 2 padronizada
        val titulo = arguments?.getString("titulo") ?: ""
        val autor = arguments?.getString("autor") ?: ""
        val isbn = arguments?.getString("isbn") ?: ""
        val capa = arguments?.getString("capa") ?: ""
        val editora = arguments?.getString("editora") ?: ""
        val sinopse = arguments?.getString("sinopse") ?: ""

        // 🌟 CORREÇÃO: Conversão limpa puxando das chaves minúsculas correspondentes
        val exemplares = arguments?.getString("exemplares")?.toIntOrNull() ?: 0
        val paginas = arguments?.getString("paginas")?.toIntOrNull() ?: 0

        btnSalvarLivro.setOnClickListener {
            val linkAudiobook = editAudiobook.text.toString().trim()
            val linkPdf = editPdf.text.toString().trim()

            // Validação de segurança opcional para URLs informadas
            if (linkAudiobook.isNotEmpty() && !linkAudiobook.startsWith("http")) {
                Toast.makeText(requireContext(), "O link do audiobook deve começar com http/https!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (linkPdf.isNotEmpty() && !linkPdf.startsWith("http")) {
                Toast.makeText(requireContext(), "O link do PDF deve começar com http/https!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSalvarLivro.isEnabled = false
            btnSalvarLivro.text = "Salvando..."

            // Montando o modelo estruturado idêntico ao banco atualizado
            val livroParaSalvar = NovoLivro(
                titulo = titulo,
                autor = autor,
                isbn = isbn,
                capaUrl = capa,
                audiobook_url = linkAudiobook,
                pdf_url = linkPdf,
                sinopse = sinopse,
                exemplares = exemplares,
                paginas = paginas,
                editora = editora
            )

            // Persistência direta no Supabase via Coroutines
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