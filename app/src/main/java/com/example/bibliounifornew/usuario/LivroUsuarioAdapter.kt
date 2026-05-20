package com.example.bibliounifornew.usuario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Livro
import com.google.android.material.button.MaterialButton

class LivroUsuarioAdapter(
    private val livros: List<Livro>,
    private val onClickLivro: (Livro) -> Unit // Agora passa o Livro clicado como parâmetro
) : RecyclerView.Adapter<LivroUsuarioAdapter.LivroViewHolder>() {

    class LivroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitulo: TextView = itemView.findViewById(R.id.textTituloLivro)
        val textAutor: TextView = itemView.findViewById(R.id.textAutorLivro)
        val textIsbn: TextView = itemView.findViewById(R.id.textIsbnLivro)
        val btnVerMais: MaterialButton = itemView.findViewById(R.id.btnVerMais)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_usuario, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = livros[position]

        holder.textTitulo.text = livro.titulo
        holder.textAutor.text = livro.autor
        holder.textIsbn.text = "ISBN: ${livro.isbn}"

        // Passa o objeto específico desta linha no clique
        holder.btnVerMais.setOnClickListener {
            onClickLivro(livro)
        }
    }

    override fun getItemCount(): Int = livros.size
}