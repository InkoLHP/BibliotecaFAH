package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF03LoginAluno
import com.google.android.material.button.MaterialButton

class TelaRF08DashboardUsuario : Fragment(R.layout.telarf08_dashboardusuario) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MAPEAMENTO DOS ÍCONES E TEXTOS
        val btnConfig = view.findViewById<ImageView>(R.id.btnConfig)
        val btnNotificacao = view.findViewById<ImageView>(R.id.btnNotificacao)
        val profileImage = view.findViewById<ImageView>(R.id.imagePerfilUsuario)
        val textNomeUsuario = view.findViewById<TextView>(R.id.textNomeUsuario)

        // MAPEAMENTO DOS BOTÕES PRINCIPAIS
        val btnPesquisa = view.findViewById<MaterialButton>(R.id.btnPesquisa)
        val btnMinhaLivraria = view.findViewById<MaterialButton>(R.id.btnMinhaLivraria)
        val btnListaDesejos = view.findViewById<MaterialButton>(R.id.btnListaDesejos)
        val btnAmigos = view.findViewById<MaterialButton>(R.id.btnAmigos)
        val btnHistorico = view.findViewById<MaterialButton>(R.id.btnHistorico)
        val btnStatusAluguel = view.findViewById<MaterialButton>(R.id.btnStatusAluguel)
        val btnSalvarAlteracoes = view.findViewById<MaterialButton>(R.id.btnSalvarAlteracoes)
        val btnSair = view.findViewById<MaterialButton>(R.id.btnSairConta)

        // AÇÕES DOS BOTÕES (Navegação)

        btnConfig?.setOnClickListener {
            irParaFragment(TelaRF09Configuracao())
        }

        btnNotificacao?.setOnClickListener {
            irParaFragment(TelaRF14Notificacoes())
        }

        btnPesquisa?.setOnClickListener {
            irParaFragment(TelaRF11TelaDePesquisa())
        }

        // Novos botões adicionados
        btnMinhaLivraria?.setOnClickListener {
            irParaFragment(telarf15_minha_livraria())
        }

        btnListaDesejos?.setOnClickListener {
            irParaFragment(telarf16_lista_desejos())
        }

        btnAmigos?.setOnClickListener {
            irParaFragment(telarf17_amigos())
        }

        // Fim dos novos botões

        btnHistorico?.setOnClickListener {
            irParaFragment(TelaRF15Historico())
        }

        btnStatusAluguel?.setOnClickListener {
            irParaFragment(TelaRF13StatusAluguel())
        }

        btnSalvarAlteracoes?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Alterações salvas com sucesso!",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnSair?.setOnClickListener {
            exibirPopupSair()
        }
    }

    // --- FUNÇÕES AUXILIARES ---

    private fun irParaFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            // Usamos o ID real que criamos na MainActivityUsuario
            .replace(R.id.fragment_container, fragment)
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
            alertDialog.dismiss()
            val intent = Intent(requireContext(), TelaRF03LoginAluno::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        alertDialog.show()
    }
}