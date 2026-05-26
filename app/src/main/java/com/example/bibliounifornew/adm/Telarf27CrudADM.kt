package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class Telarf27CrudADM : Fragment(R.layout.telarf27_crud_adm) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Mapeando os botões do XML
        val buttonCriarMidia = view.findViewById<MaterialButton>(R.id.buttonCriarMidia)
        val buttonVerificarMidia = view.findViewById<MaterialButton>(R.id.buttonVerificarMidia)
        val buttonGerenciarUsuarios = view.findViewById<MaterialButton>(R.id.buttonGerenciarUsuarios)

        // 2. Ação do Botão: Criar Mídia
        buttonCriarMidia.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf33AdicionarMidiaArquivos())
                .addToBackStack(null)
                .commit()
        }

        // 3. Ação do Botão: Verificar Mídia
        buttonVerificarMidia.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf32LivrosCrudADM())
                .addToBackStack(null)
                .commit()
        }

        // 4. Ação do Botão: Gerenciar Usuários
        buttonGerenciarUsuarios.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf29GerenciamentoUsuariosADM())
                .addToBackStack(null)
                .commit()
        }
    }
}