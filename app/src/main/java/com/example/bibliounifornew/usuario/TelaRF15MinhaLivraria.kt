package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R

class TelaRF15MinhaLivraria : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.telarf15_minha_livraria, container, false)
    }

    // 👇 NOVO: Atualiza a foto se ela existir nessa tela
    override fun onResume() {
        super.onResume()
        val sharedPref = requireActivity().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
        val fotoUsuarioUri = sharedPref.getString("USER_FOTO", null)
        val profileImage = view?.findViewById<ImageView>(R.id.imagePerfilUsuario)

        if (profileImage != null && !fotoUsuarioUri.isNullOrBlank()) {
            Glide.with(this).load(fotoUsuarioUri).circleCrop().into(profileImage)
        }
    }
}