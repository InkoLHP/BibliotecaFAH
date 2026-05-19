package com.example.bibliounifornew.usuario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class LivroUsuarioAdapter(
    private val livros: List<Livro>,
    private val onClickLivro: () -> Unit
) : RecyclerView.Adapter<LivroUsuarioAdapter.LivroViewHolder>() {

    class LivroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textTitulo: TextView =
            itemView.findViewById(R.id.textTituloLivro)

        val textAutor: TextView =
            itemView.findViewById(R.id.textAutorLivro)

        val textIsbn: TextView =
            itemView.findViewById(R.id.textIsbnLivro)

        val btnVerMais: MaterialButton =
            itemView.findViewById(R.id.btnVerMais)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_midia_usuario, parent, false)

        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {

        val livro = livros[position]

        holder.textTitulo.text = livro.titulo
        holder.textAutor.text = livro.autor
        holder.textIsbn.text = "ISBN: ${livro.isbn}"

        holder.btnVerMais.setOnClickListener {
            onClickLivro()
        }
    }

    override fun getItemCount(): Int {
        return livros.size
    }
}