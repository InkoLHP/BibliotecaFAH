package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R

class TelaRF34FinanceiroADM : Fragment(R.layout.telarf34_finaceiro_adm) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adicionando 'view.' para buscar o botão dentro do layout do Fragment
        val buttonVerPendentes = view.findViewById<Button>(R.id.btnVerPendentes)

        // tem que fazer a integração dessa tela com o banco de dados, separar o card de livro aí e fazer o sisteminha de lista dinâmica eu creio

        buttonVerPendentes.setOnClickListener {
            // lógica de quando apertar o botão btnVerPendentes
        }
    }
}