package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R

class TelaRF39RedefinirADMInterno : Fragment(R.layout.telarf39_redefinir_adm_interno) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adicionado 'view.' antes do findViewById
        val etSenha = view.findViewById<EditText>(R.id.editTextTextPassword)
        val etSenhaConfirmacao = view.findViewById<EditText>(R.id.editTextTextPasswordConfirmacao)
        val bntX = view.findViewById<TextView>(R.id.buttonX) // Alterado para TextView para evitar crash
        val bntSalvar = view.findViewById<Button>(R.id.buttonSalvar)

        bntSalvar.setOnClickListener {
            val s1 = etSenha.text.toString()
            val s2 = etSenhaConfirmacao.text.toString()

            if (s1 == s2 && s1.isNotEmpty()) {
                // Toast agora usa requireContext() em vez de 'this'
                Toast.makeText(requireContext(), "Senha redefinida com sucesso!", Toast.LENGTH_SHORT).show()

                // Em Fragments, fechamos a tela "desempilhando" ela, em vez de usar finish()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            }
        }

        bntX.setOnClickListener {
            // Volta para a tela anterior
            parentFragmentManager.popBackStack()
        }
    }
}