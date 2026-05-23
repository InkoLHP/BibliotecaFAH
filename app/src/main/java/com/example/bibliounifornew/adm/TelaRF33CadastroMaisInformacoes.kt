package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R

class TelaRF33CadastroMaisInformacoes : Fragment(R.layout.telarf33_cadastro_mais_informacoes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adicionando o 'view.' antes do findViewById para referenciar o layout do Fragment
        val editPaginas = view.findViewById<EditText>(R.id.etPaginas)
        val editCategoria = view.findViewById<EditText>(R.id.etCategoria)
        val editEditora = view.findViewById<EditText>(R.id.etEditora)
        val editCapa = view.findViewById<EditText>(R.id.etCapa)
        val editSinopse = view.findViewById<EditText>(R.id.etSinopse)

        // ainda n sei se vai ter a tela versões, então sem botão aqui por enquanto

    }
}