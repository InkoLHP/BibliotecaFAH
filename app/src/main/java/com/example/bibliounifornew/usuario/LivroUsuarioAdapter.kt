package com.example.bibliounifornew.usuario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load // Importação do Coil
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Livro
import com.google.android.material.button.MaterialButton

class LivroUsuarioAdapter(
    private val livros: List<Livro>,
    private val onClickVerMais: (Livro) -> Unit // Função ativada ao clicar no botão
) : RecyclerView.Adapter<LivroUsuarioAdapter.LivroViewHolder>() {

    class LivroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitulo: TextView = itemView.findViewById(R.id.textTituloLivro)
        val textAutor: TextView = itemView.findViewById(R.id.textAutorLivro)
        val textIsbn: TextView = itemView.findViewById(R.id.textIsbnLivro)

        // Mapeando a imagem e o botão do seu layout
        val imagemCapa: ImageView = itemView.findViewById(R.id.imgCapaLivro) // Substitua pelo ID real da sua capa no XML
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

        // A MÁGICA DO COIL AQUI:
        // Ele vai pegar a URL do Supabase e carregar a imagem na ImageView automaticamente
        holder.imagemCapa.load(livro.capaUrl) {
            crossfade(true) // Faz uma transição suave ao carregar
            // Você pode colocar a capa do "Alienista" ou outra genérica como placeholder
            // caso a internet esteja lenta ou o livro não tenha foto no banco
            placeholder(R.drawable.o_alienista_capa)
            error(R.drawable.o_alienista_capa) // Mostra se a URL estiver quebrada
        }

        // O clique no botão "Ver mais" aciona a abertura das opções
        holder.btnVerMais.setOnClickListener {
            onClickVerMais(livro)
        }
    }

    override fun getItemCount(): Int = livros.size
}