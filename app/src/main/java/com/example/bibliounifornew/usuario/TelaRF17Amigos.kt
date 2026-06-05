package com.example.bibliounifornew.usuario

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import coil.load
import com.example.bibliounifornew.R

class TelaRF17Amigos : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.telarf17_amigos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Carrega os dados de perfil assim que a view é criada
        carregarDadosPerfil(view)
    }

    override fun onResume() {
        super.onResume()
        // Garante a atualização em tempo real caso venha de outra tela (como a de configurações)
        view?.let { carregarDadosPerfil(it) }
    }

    private fun carregarDadosPerfil(viewContainer: View) {
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val fotoUsuarioUri = sharedPref.getString("USER_FOTO", null)
        val nomeUsuario = sharedPref.getString("USER_NOME", "Usuário")

        // 📸 Mapeia e carrega a foto de perfil com Coil
        val profileImage = viewContainer.findViewById<ImageView>(R.id.imageUsuarioAmigos)
        if (profileImage != null && !fotoUsuarioUri.isNullOrBlank()) {
            profileImage.load(fotoUsuarioUri) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        // 📝 Mapeia e insere o Nome do Usuário dinamicamente abaixo da foto
        val textNome = viewContainer.findViewById<TextView>(R.id.textUsuarioAmigos)
        textNome?.text = nomeUsuario
    }
}