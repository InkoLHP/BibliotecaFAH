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
            if (editPaginas.text.isBlank() || editCategoria.text.isBlank() || editEditora.text.isBlank()) {
                Toast.makeText(requireContext(), "Preencha as páginas, categoria e editora!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val linkCapa = editCapa.text.toString().trim()
            if (linkCapa.isNotEmpty() && !linkCapa.startsWith("http")) {
                Toast.makeText(requireContext(), "O link da capa deve começar com 'http' ou 'https'!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // CORRIGIDO: Agora lê em minúsculas batendo com a Tela 1
            val titulo = arguments?.getString("titulo") ?: ""
            val autor = arguments?.getString("autor") ?: ""
            val isbn = arguments?.getString("isbn") ?: ""
            val data = arguments?.getString("data") ?: ""
            val exemplares = arguments?.getString("exemplares") ?: ""

            // Monta o pacote para a Tela 3 com chaves unificadas
            val fragment = Telarf33AdicionarMidiaArquivos().apply {
                arguments = Bundle().apply {
                    putString("titulo", titulo)
                    putString("autor", autor)
                    putString("isbn", isbn)
                    putString("data", data)
                    putString("exemplares", exemplares)
                    putString("paginas", editPaginas.text.toString().trim())
                    putString("categoria", editCategoria.text.toString().trim())
                    putString("editora", editEditora.text.toString().trim())
                    putString("capa", linkCapa)
                    putString("sinopse", editSinopse.text.toString().trim())
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}