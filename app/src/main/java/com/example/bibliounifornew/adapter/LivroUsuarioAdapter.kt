package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Livro
import com.google.android.material.button.MaterialButton

class LivroUsuarioAdapter(
    private val livros: List<Livro>,
    private val onVerMaisClick: (Livro) -> Unit,
    private val onAddListaDesejosClick: (Livro) -> Unit,
    private val onAddMinhaLivrariaClick: (Livro) -> Unit
) : RecyclerView.Adapter<LivroUsuarioAdapter.LivroViewHolder>() {

    class LivroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapaLivro: ImageView = itemView.findViewById(R.id.imgCapaLivro)
        val textTituloLivro: TextView = itemView.findViewById(R.id.textTituloLivro)
        val textAutorLivro: TextView = itemView.findViewById(R.id.textAutorLivro)
        val textIsbnLivro: TextView = itemView.findViewById(R.id.textIsbnLivro)

        // Novos botões do XML mapeados
        val btnAddListaDesejos: MaterialButton = itemView.findViewById(R.id.btnAddListaDesejos)
        val btnAddMinhaLivraria: MaterialButton = itemView.findViewById(R.id.btnAddMinhaLivraria)
        val btnVerMais: MaterialButton = itemView.findViewById(R.id.btnVerMais)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_usuario, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = livros[position]

        holder.textTituloLivro.text = livro.titulo ?: "Sem título"
        holder.textAutorLivro.text = livro.autor ?: "Autor desconhecido"
        holder.textIsbnLivro.text = "ISBN: ${livro.isbn ?: "N/A"}"

        Glide.with(holder.itemView.context)
            .load(livro.capaUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.imgCapaLivro)

        // Configuração dos cliques individuais
        holder.btnVerMais.setOnClickListener {
            onVerMaisClick(livro)
        }

        holder.itemView.setOnClickListener {
            onVerMaisClick(livro)
        }

        holder.btnAddListaDesejos.setOnClickListener {
            onAddListaDesejosClick(livro)
        }

        holder.btnAddMinhaLivraria.setOnClickListener {
            onAddMinhaLivrariaClick(livro)
        }
    }

    override fun getItemCount(): Int = livros.size
}