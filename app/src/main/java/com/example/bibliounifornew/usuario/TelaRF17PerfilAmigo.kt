package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import coil.load
import com.example.bibliounifornew.R

class TelaRF17PerfilAmigo : Fragment(R.layout.telarf17_5_perfil_amigo) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mapeia os componentes do seu XML de perfil
        val imagePerfilAmigo = view.findViewById<ImageView>(R.id.imagePerfilAmigo)
        val textEmailPerfilAmigo = view.findViewById<TextView>(R.id.textEmailPerfilAmigo)
        val textNomePerfilAmigo = view.findViewById<TextView>(R.id.textNomePerfilAmigo)
        val textUsuarioPerfilAmigo = view.findViewById<TextView>(R.id.textUsuarioPerfilAmigo)
        val textBioPerfilAmigo = view.findViewById<TextView>(R.id.textBioPerfilAmigo)

        // Recupera os dados que foram passados pelo Bundle da tela anterior
        val amigoNome = arguments?.getString("AMIGO_NOME")
        val amigoEmail = arguments?.getString("AMIGO_EMAIL")
        val amigoFoto = arguments?.getString("AMIGO_FOTO")
        val amigoBio = arguments?.getString("AMIGO_BIO")

        // Injeta os textos nos componentes
        textNomePerfilAmigo.text = amigoNome ?: "Sem Nome"
        textEmailPerfilAmigo.text = amigoEmail ?: "Sem E-mail"
        textUsuarioPerfilAmigo.text = amigoEmail?.substringBefore("@") ?: "usuario"
        textBioPerfilAmigo.text = if (!amigoBio.isNullOrEmpty()) amigoBio else "Este usuário não informou uma biografia."

        // Carrega a foto do usuário clicado usando o Coil
        if (!amigoFoto.isNullOrEmpty()) {
            imagePerfilAmigo.load(amigoFoto) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        } else {
            imagePerfilAmigo.setImageResource(R.drawable.user_placeholder)
        }
    }
}