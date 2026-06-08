package com.example.bibliounifornew.adm

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Livro

class TelaRF12TelaDoLivroADM : Fragment(R.layout.telarf12_tela_livro_adm) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Recebe o objeto Livro enviado pelas telas de lista (Aluguéis, etc.)
        val livro = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("livro", Livro::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("livro") as? Livro
        }

        // Se o livro carregou corretamente, preenchemos a tela e ativamos o botão
        if (livro != null) {
            mostrarLivro(view, livro)

            // =========================================================
            // 🌟 BOTÃO DE EDITAR: Direcionando para a TelaRF37EditarMidia
            // =========================================================
            val btnEditarLivro = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonEditarInformacoes)

            btnEditarLivro?.setOnClickListener {
                val fragmentEditar = TelaRF37EditarMidia().apply {
                    arguments = Bundle().apply {
                        // Passa o ID para a tela de edição buscar no Supabase
                        putString("LIVRO_ID", livro.id)
                    }
                }

                // Troca de tela e permite voltar clicando na seta de voltar do celular
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragmentEditar)
                    .addToBackStack(null)
                    .commit()
            }

        } else {
            Toast.makeText(requireContext(), "Erro ao carregar os detalhes do livro.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun mostrarLivro(view: View, livro: Livro) {
        // Preenchendo textos e imagens básicas
        view.findViewById<TextView>(R.id.textTituloLivro).text = livro.titulo
        view.findViewById<TextView>(R.id.textAutorLivro).text = livro.autor
        view.findViewById<TextView>(R.id.textSobreLivro).text = livro.sinopse ?: "Sinopse não disponível."

        // Carrega a imagem da capa usando Coil
        view.findViewById<ImageView>(R.id.imageLivroDetalhes).load(livro.capaUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        // Preenchendo campos que vieram no modelo Livro
        view.findViewById<TextView>(R.id.textGeneroLivro).text = livro.categoria ?: "N/I"
        view.findViewById<TextView>(R.id.textDataLivro).text = livro.data_publicacao ?: "N/I"
        view.findViewById<TextView>(R.id.textIsbnLivro).text = livro.isbn ?: "N/I"

        // Preenchendo os campos removidos do modelo direto com "N/I"
        view.findViewById<TextView>(R.id.textIdiomaLivro).text = "N/I"
        view.findViewById<TextView>(R.id.textEditoraLivro).text = "N/I"
        view.findViewById<TextView>(R.id.textDimensaoLivro).text = "N/I"
        view.findViewById<TextView>(R.id.textPaginasLivro).text = "N/I"

        // Verifica disponibilidade de PDF / Digital
        val eDigital = (livro.formato ?: "").contains("pdf", ignoreCase = true) ||
                (livro.formato ?: "").contains("epub", ignoreCase = true) ||
                !livro.pdfUrl.isNullOrEmpty()

        view.findViewById<TextView>(R.id.textPdfDisponivel).text = if (eDigital) "Sim" else "Não"
    }
}