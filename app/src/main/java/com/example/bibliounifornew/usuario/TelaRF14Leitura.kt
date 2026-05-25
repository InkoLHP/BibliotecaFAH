package com.example.bibliounifornew.usuario

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Livro
import com.google.android.material.button.MaterialButton

class TelaRF14Leitura : Fragment(R.layout.telarf14_leitura) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o seu layout XML premium
        return inflater.inflate(R.layout.telarf14_leitura, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera o livro passado da tela anterior (agora através de um Bundle/Arguments)
        val livro = arguments?.getSerializable("livro") as? Livro

        // 1. MAPEAMENTO DO HEADER PREMIUM (Note o uso do 'view.findViewById')
        val imageCapa = view.findViewById<ImageView>(R.id.imageLivroAcoes)
        val textTitulo = view.findViewById<TextView>(R.id.textTituloLivroAcoes)
        val textAutor = view.findViewById<TextView>(R.id.textAutorLivroAcoes)
        val textCategoria = view.findViewById<TextView>(R.id.textCategoriaLivroAcoes)

        if (livro != null) {
            textTitulo.text = livro.titulo ?: "Sem título"
            textAutor.text = livro.autor ?: "Autor desconhecido"
            textCategoria.text = livro.categoria ?: "Categoria não informada"

            imageCapa.load(livro.capaUrl) {
                crossfade(true)
                placeholder(R.drawable.o_alienista_capa)
                error(R.drawable.o_alienista_capa)
            }
        } else {
            Toast.makeText(requireContext(), "Erro ao carregar os dados do livro.", Toast.LENGTH_SHORT).show()
            // Volta para a tela anterior em caso de erro
            parentFragmentManager.popBackStack()
            return
        }

        // 2. MAPEAMENTO DOS BOTÕES DE AÇÃO
        val btnAlugar = view.findViewById<MaterialButton>(R.id.buttonAlugarLivro)
        val btnProcurar = view.findViewById<MaterialButton>(R.id.buttonProcurarLivro)
        val btnAbrirPdf = view.findViewById<MaterialButton>(R.id.buttonAbrirPdfLivro)
        val btnAbrirAudio = view.findViewById<MaterialButton>(R.id.buttonAbrirAudioLivro)
        val btnReservar = view.findViewById<MaterialButton>(R.id.buttonReservarLivro)

        // 3. CONFIGURAÇÃO DOS CLIQUES
        btnAlugar.setOnClickListener {
            if (livro.disponivel) {
                Toast.makeText(requireContext(), "Livro disponível! Iniciando processo...", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Aviso: Este livro está indisponível para aluguel no momento.", Toast.LENGTH_LONG).show()
            }
        }

        btnProcurar.setOnClickListener {
            // Como é um app 100% Fragment, nós limpamos a pilha para voltar à Tela de Pesquisa
            requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        btnAbrirPdf.setOnClickListener {
            if (!livro.pdfUrl.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Abrindo leitor oficial do Google Books...", Toast.LENGTH_SHORT).show()
                val intentNavegador = Intent(Intent.ACTION_VIEW, Uri.parse(livro.pdfUrl))
                startActivity(intentNavegador)
            } else {
                Toast.makeText(requireContext(), "Este livro não possui versão digital disponível.", Toast.LENGTH_SHORT).show()
            }
        }

        btnAbrirAudio.setOnClickListener {
            Toast.makeText(requireContext(), "Audiobook não disponível no momento para este livro.", Toast.LENGTH_SHORT).show()
        }

        btnReservar.setOnClickListener {
            Toast.makeText(requireContext(), "Função de reserva ficará disponível em breve.", Toast.LENGTH_SHORT).show()
        }
    }
}