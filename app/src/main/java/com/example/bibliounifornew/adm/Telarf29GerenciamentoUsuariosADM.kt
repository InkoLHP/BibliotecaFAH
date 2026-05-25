package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R

class Telarf29GerenciamentoUsuariosADM : Fragment(R.layout.telarf29_gerenciamento_usuarios_adm) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Linkando o nome e o ícone do primeiro usuário (Ronaldo Alves)
        val textUsuario1 = view.findViewById<TextView>(R.id.textUsuario1)
        val viewVerUsuario = view.findViewById<ImageView>(R.id.viewVerUsuario)

        // 2. Criando a ação de navegação
        val acaoClicarUsuario = View.OnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf30UsuariosADM())
                .addToBackStack(null) // Permite voltar usando o botão do celular
                .commit()
        }

        // 3. Ativando o clique no nome E no ícone
        textUsuario1.setOnClickListener(acaoClicarUsuario)
        viewVerUsuario.setOnClickListener(acaoClicarUsuario)
    }
}