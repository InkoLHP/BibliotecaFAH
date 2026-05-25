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

class Telarf33AdicionarMidiaArquivos : Fragment(R.layout.telarf33_adicionar_midia_arquivos) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Pegando os componentes da tela atual
        val editPdf = view.findViewById<EditText>(R.id.editArquivoPdf)
        val editAudiobook = view.findViewById<EditText>(R.id.editArquivoAudiobook)
        val checkBraille = view.findViewById<CheckBox>(R.id.checkBraille)
        val btnSalvarLivro = view.findViewById<MaterialButton>(R.id.btnSalvarLivro)

        // 2. Pegando os dados que vieram guardados das telas anteriores
        val titulo = arguments?.getString("TITULO") ?: ""
        val autor = arguments?.getString("AUTOR") ?: ""
        val isbn = arguments?.getString("ISBN") ?: ""
        val data = arguments?.getString("DATA") ?: ""
        val exemplares = arguments?.getString("EXEMPLARES") ?: ""
        val paginas = arguments?.getString("PAGINAS") ?: ""
        val categoria = arguments?.getString("CATEGORIA") ?: ""
        val editora = arguments?.getString("EDITORA") ?: ""
        val capa = arguments?.getString("CAPA") ?: ""
        val sinopse = arguments?.getString("SINOPSE") ?: ""

        // 3. Quando clicar em Salvar
        btnSalvarLivro.setOnClickListener {
            btnSalvarLivro.isEnabled = false // Desativa para evitar duplo clique

            // Juntamos todas as 13 informações em um "mapa" para enviar pro banco
            val dadosDoLivro = mapOf(
                "titulo" to titulo,
                "autor" to autor,
                "isbn" to isbn,
                "data_publicacao" to data,
                "exemplares" to exemplares,
                "paginas" to paginas,
                "categoria" to categoria,
                "editora" to editora,
                "capa_url" to capa,
                "sinopse" to sinopse,
                "pdf_url" to editPdf.text.toString().trim(),
                "audiobook_url" to editAudiobook.text.toString().trim(),
                "disponivel_braille" to checkBraille.isChecked
            )

            // Envia para o Supabase
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        // Enviando o mapa direto para a tabela "livros"
                        SupabaseConfig.client.postgrest["livros"].insert(dadosDoLivro)
                    }
                    // Se deu certo, mostra o pop-up que você criou
                    mostrarPopupSucesso()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                    btnSalvarLivro.isEnabled = true
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
            // Volta para a tela inicial do seu CRUD do ADM
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf27CrudADM())
                .commit()
        }
        dialog.show()
    }
}