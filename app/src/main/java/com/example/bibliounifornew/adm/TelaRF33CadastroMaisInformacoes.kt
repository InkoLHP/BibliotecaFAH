package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF33CadastroMaisInformacoes : Fragment(R.layout.telarf33_cadastro_mais_informacoes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editPaginas = view.findViewById<EditText>(R.id.etPaginas)
        val editCategoria = view.findViewById<EditText>(R.id.etCategoria)
        val editEditora = view.findViewById<EditText>(R.id.etEditora)
        val editCapa = view.findViewById<EditText>(R.id.etCapa)
        val editSinopse = view.findViewById<EditText>(R.id.etSinopse)
        val btnIrVersoes = view.findViewById<MaterialButton>(R.id.btnIrVersoes)

        btnIrVersoes.setOnClickListener {

            // 1. Validação de campos vazios
            if (editPaginas.text.isBlank() || editCategoria.text.isBlank() || editEditora.text.isBlank()) {
                Toast.makeText(requireContext(), "Preencha as páginas, categoria e editora!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ==========================================
            // 👇 AQUI ENTRA A VALIDAÇÃO DO LINK DA CAPA 👇
            // ==========================================
            val linkCapa = editCapa.text.toString().trim()
            if (linkCapa.isNotEmpty() && !linkCapa.startsWith("http")) {
                Toast.makeText(requireContext(), "O link da capa deve começar com 'http' ou 'https'!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Trava a tela e não deixa avançar
            }
            // ==========================================

            // Resgatar os dados da Tela 1
            val tituloAntigo = arguments?.getString("TITULO") ?: ""
            val autorAntigo = arguments?.getString("AUTOR") ?: ""
            val isbnAntigo = arguments?.getString("ISBN") ?: ""
            val dataAntiga = arguments?.getString("DATA") ?: ""
            val exemplaresAntigo = arguments?.getString("EXEMPLARES") ?: ""

            // Prepara a Tela 3 com todos os dados
            val fragment = Telarf33AdicionarMidiaArquivos().apply {
                arguments = Bundle().apply {
                    putString("TITULO", tituloAntigo)
                    putString("AUTOR", autorAntigo)
                    putString("ISBN", isbnAntigo)
                    putString("DATA", dataAntiga)
                    putString("EXEMPLARES", exemplaresAntigo)
                    putString("PAGINAS", editPaginas.text.toString().trim())
                    putString("CATEGORIA", editCategoria.text.toString().trim())
                    putString("EDITORA", editEditora.text.toString().trim())
                    putString("CAPA", linkCapa) // Manda o link que foi validado
                    putString("SINOPSE", editSinopse.text.toString().trim())
                }
            }

            // Navega para a última tela
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}