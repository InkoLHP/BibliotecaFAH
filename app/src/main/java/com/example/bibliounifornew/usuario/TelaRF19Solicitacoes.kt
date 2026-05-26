package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*

class TelaRF19Solicitacoes : Fragment(R.layout.telarf19_solicitacoes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textTitulo = view.findViewById<TextView>(R.id.textTituloLivroSolicitacao)
        val textAutor = view.findViewById<TextView>(R.id.textAutorLivroSolicitacao)

        val btnPdf = view.findViewById<MaterialButton>(R.id.buttonSolicitarPdf)
        val btnBraille = view.findViewById<MaterialButton>(R.id.buttonSolicitarBraille)
        val btnAudiobook = view.findViewById<MaterialButton>(R.id.buttonSolicitarAudiobook)
        val btnReservar = view.findViewById<MaterialButton>(R.id.buttonReservarLivro)
        val btnSetor = view.findViewById<MaterialButton>(R.id.buttonSetorLocalizado)

        // Botões enviando o tipo exato da solicitação
        btnPdf.setOnClickListener { enviarSolicitacao(textTitulo.text.toString(), textAutor.text.toString(), "PDF") }
        btnBraille.setOnClickListener { enviarSolicitacao(textTitulo.text.toString(), textAutor.text.toString(), "Braille") }
        btnAudiobook.setOnClickListener { enviarSolicitacao(textTitulo.text.toString(), textAutor.text.toString(), "Audiobook") }
        btnReservar.setOnClickListener { enviarSolicitacao(textTitulo.text.toString(), textAutor.text.toString(), "Reserva") }
        btnSetor.setOnClickListener { enviarSolicitacao(textTitulo.text.toString(), textAutor.text.toString(), "Setor Localizado") }
    }

    private fun enviarSolicitacao(titulo: String, autor: String, tipo: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Monta o objeto com os nomes EXATOS da sua classe Solicitacao.kt
                val novaSolicitacao = Solicitacao(
                    titulo = titulo,
                    autor = autor,
                    usuario_nome = "Usuário Atual", // Mais pra frente você puxa o nome do usuário logado
                    tipo_solicitacao = tipo,
                    status = "Pendente"
                )

                // Envia pro banco
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["solicitacoes"].insert(novaSolicitacao)
                }

                Toast.makeText(requireContext(), "Solicitação de $tipo enviada!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao enviar solicitação", Toast.LENGTH_SHORT).show()
            }
        }
    }
}