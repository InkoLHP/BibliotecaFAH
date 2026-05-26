package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class Telarf30UsuariosADM : Fragment(R.layout.telarf30_usuarios_adm) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Linkando o botão "Livros Alugados"
        val buttonLivrosAlugados = view.findViewById<MaterialButton>(R.id.buttonLivrosAlugados)

        // 2. Ação de clique
        buttonLivrosAlugados.setOnClickListener {

            // Navegando para a Tela de Alugados do Usuário
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf30UsuarioAlugadosADM())
                .addToBackStack(null) // Salva o histórico para o botão "Voltar" do celular funcionar
                .commit()
        }
    }
}