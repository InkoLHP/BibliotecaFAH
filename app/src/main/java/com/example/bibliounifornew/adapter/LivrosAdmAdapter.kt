package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adm.LivroCadastrado
import com.google.android.material.button.MaterialButton

class LivrosAdmAdapter(
    private var livros: List<LivroCadastrado>,
    private val onEditarClick: (LivroCadastrado) -> Unit
) : RecyclerView.Adapter<LivrosAdmAdapter.LivroViewHolder>() {

    class LivroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.textTituloLivro)
        val autor: TextView = view.findViewById(R.id.textAutorLivro)
        val isbn: TextView = view.findViewById(R.id.textIsbnLivro)
        val imgCapa: ImageView = view.findViewById(R.id.imgCapaLivro)
        val btnEditar: MaterialButton = view.findViewById(R.id.btnEditarInformacoes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_livro_adm, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = livros[position]

        holder.titulo.text = livro.titulo ?: "Sem título"
        holder.autor.text = livro.autor ?: "Autor desconhecido"
        holder.isbn.text = "ISBN: ${livro.isbn ?: "N/A"}"

        // 🚀 Padronizado: Agora usa Coil em vez de Glide
        holder.imgCapa.load(livro.capaUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        holder.btnEditar.setOnClickListener {
            onEditarClick(livro)
        }
    }

    override fun getItemCount(): Int = livros.size

    fun atualizarLista(novaLista: List<LivroCadastrado>) {
        livros = novaLista
        notifyDataSetChanged()
    }
}
