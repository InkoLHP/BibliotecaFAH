package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class Telarf32LivrosCrudADM : Fragment(R.layout.telarf32_livros_crud_adm) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Linkando o botão do XML com o Kotlin
        val btnEditarInformacoes = view.findViewById<MaterialButton>(R.id.btnEditarInformacoes)

        // 2. Ação de clique
        btnEditarInformacoes.setOnClickListener {

            // Navegando para a TelaRF37EditarMidia
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, TelaRF37EditarMidia())
                .addToBackStack(null) // Salva o histórico para o botão "Voltar" do celular funcionar
                .commit()
        }
    }
}