package com.example.bibliounifornew.usuario

import android.content.Intent
import android.net.Uri // 🌟 Importante para ler o caminho da imagem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF03LoginAluno
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class TelaRF08DashboardUsuario : Fragment(R.layout.telarf08_dashboardusuario) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MAPEAMENTO DOS ÍCONES E TEXTOS
        val btnConfig = view.findViewById<ImageView>(R.id.btnConfig)
        val btnNotificacao = view.findViewById<ImageView>(R.id.btnNotificacao)
        val profileImage = view.findViewById<ImageView>(R.id.imagePerfilUsuario)
        val textNomeUsuario = view.findViewById<TextView>(R.id.textNomeUsuario)

        val sharedPref = requireActivity().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)

        val nomeUsuario = sharedPref.getString("USER_NOME", "Usuário")
        textNomeUsuario.text = nomeUsuario ?: "Usuário"

        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)
        if (!fotoSalvaUrl.isNullOrEmpty()) {
            try {
                profileImage?.setImageURI(Uri.parse(fotoSalvaUrl))
            } catch (e: Exception) {
                e.printStackTrace() // Evita crash caso a foto original suma do celular
            }
        }

        // MAPEAMENTO DOS BOTÕES PRINCIPAIS
        val btnPesquisa = view.findViewById<MaterialButton>(R.id.btnPesquisa)
        val btnMinhaLivraria = view.findViewById<MaterialButton>(R.id.btnMinhaLivraria)
        val btnAmigos = view.findViewById<MaterialButton>(R.id.btnAmigos)
        val btnHistorico = view.findViewById<MaterialButton>(R.id.btnHistorico)
        val btnStatusAluguel = view.findViewById<MaterialButton>(R.id.btnStatusAluguel)
        val btnSair = view.findViewById<MaterialButton>(R.id.btnSairConta)

        // REFERÊNCIA À BOTTOM NAV DA ACTIVITY
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // AÇÕES DOS BOTÕES (Navegação)
        btnConfig.setOnClickListener {
            val emailUsuario = sharedPref.getString("USER_EMAIL", null)
            val fragment = TelaRF09Configuracao().apply {
                arguments = Bundle().apply { putString("USER_EMAIL", emailUsuario) }
            }
            irParaFragment(fragment)
        }

        btnNotificacao?.setOnClickListener {
            irParaFragment(TelaRF14Notificacoes())
        }

        btnPesquisa?.setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_pesquisa
        }

        btnMinhaLivraria?.setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_livraria
        }

        btnAmigos?.setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_amigos
        }

        btnHistorico?.setOnClickListener {
            irParaFragment(TelaRF15Historico())
        }

        btnStatusAluguel?.setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_status
        }

        btnSair?.setOnClickListener {
            exibirPopupSair()
        }
    }

    // --- FUNÇÕES AUXILIARES ---

    private fun irParaFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun exibirPopupSair() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_sair_conta, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnConfirmarSair = dialogView.findViewById<MaterialButton>(R.id.btnConfirmarSair)

        btnConfirmarSair?.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            alertDialog.dismiss()

            val intent = Intent(requireContext(), TelaRF03LoginAluno::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        alertDialog.show()
    }
}