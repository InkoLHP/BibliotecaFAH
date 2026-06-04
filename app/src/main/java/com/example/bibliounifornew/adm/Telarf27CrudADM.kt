package com.example.bibliounifornew.adm

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class Telarf27CrudADM : Fragment(R.layout.telarf27_crud_adm) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🌟 NOVO: Recuperando a foto do ADM salva na sessão (SharedPreferences)
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val urlFoto = sharedPref.getString("USER_FOTO", null)

        // 🌟 NOVO: Mapeando a ImageView e carregando com o Coil
        val imagePerfilCrudAdm = view.findViewById<ImageView>(R.id.imagePerfilCrudAdm)
        if (!urlFoto.isNullOrEmpty()) {
            imagePerfilCrudAdm.load(urlFoto) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        // 1. Mapeando os botões do XML
        val buttonCriarMidia = view.findViewById<MaterialButton>(R.id.buttonCriarMidia)
        val buttonVerificarMidia = view.findViewById<MaterialButton>(R.id.buttonVerificarMidia)
        val buttonGerenciarUsuarios = view.findViewById<MaterialButton>(R.id.buttonGerenciarUsuarios)

        // 2. Ação do Botão: Criar Mídia
        buttonCriarMidia.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, TelaRF33CadastroDeLivros())
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