package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class LivroUsuriousAdapter(
    private val livros: List<Livro>,
    private val onClick: (Livro) -> Unit
) : RecyclerView.Adapter<LivroUsuriousAdapter.LivroViewHolder>() {

    class LivroViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imgCapaLivro: ImageView = itemView.findViewById(R.id.imgCapaLivro)
        val textTituloLivro: TextView = itemView.findViewById(R.id.textTituloLivro)
        val textAutorLivro: TextView = itemView.findViewById(R.id.textAutorLivro)
        val textIsbnLivro: TextView = itemView.findViewById(R.id.textIsbnLivro)
        val btnVerMais: MaterialButton = itemView.findViewById(R.id.btnVerMais)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_usuario, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LivroViewHolder,
        position: Int
    ) {
        val livro = livros[position]

        holder.textTituloLivro.text = livro.titulo
        holder.textAutorLivro.text = livro.autor
        holder.textIsbnLivro.text = livro.isbn

        Glide.with(holder.itemView.context)
            .load(livro.capaUrl)
            .placeholder(R.drawable.o_alienista_capa)
            .into(holder.imgCapaLivro)

        holder.btnVerMais.setOnClickListener {
            onClick(livro)
        }

        holder.itemView.setOnClickListener {
            onClick(livro)
        }
    }

    override fun getItemCount(): Int = livros.size
}

class LivroUsuarioAdapter(
    private val livros: List<Livro>,
    private val onClick: (Livro) -> Unit
) : RecyclerView.Adapter<LivroUsuarioAdapter.LivroViewHolder>() {

    class LivroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapaLivro: ImageView = itemView.findViewById(R.id.imgCapaLivro)
        val textTituloLivro: TextView = itemView.findViewById(R.id.textTituloLivro)
        val textAutorLivro: TextView = itemView.findViewById(R.id.textAutorLivro)
        val textIsbnLivro: TextView = itemView.findViewById(R.id.textIsbnLivro)
        val btnVerMais: MaterialButton = itemView.findViewById(R.id.btnVerMais)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_usuario, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = livros[position]

        holder.textTituloLivro.text = livro.titulo
        holder.textAutorLivro.text = livro.autor
        holder.textIsbnLivro.text = livro.isbn

        Glide.with(holder.itemView.context)
            .load(livro.capaUrl)
            .placeholder(R.drawable.o_alienista_capa)
            .into(holder.imgCapaLivro)

        holder.btnVerMais.setOnClickListener {
            onClick(livro)
        }

        holder.itemView.setOnClickListener {
            onClick(livro)
        }
    }

    override fun getItemCount(): Int = livros.size
}