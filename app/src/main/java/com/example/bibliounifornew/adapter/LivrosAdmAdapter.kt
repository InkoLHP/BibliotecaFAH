package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class LivrosAdmAdapter(
    private var livros: List<LivroCadastrado>,
    private val onEditarClick: (LivroCadastrado) -> Unit
) : RecyclerView.Adapter<LivrosAdmAdapter.LivroViewHolder>() {

    class LivroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.textTituloLivro)
        val autor: TextView = view.findViewById(R.id.textAutorLivro)
        val isbn: TextView = view.findViewById(R.id.textIsbnLivro)
        val imgCapa: ImageView = view.findViewById(R.id.imgCapaLivro) // Mapeamos a foto da capa
        val btnEditar: MaterialButton = view.findViewById(R.id.btnEditarInformacoes)
    }

    // CORRIGIDO: Agora infla o card_livro_adm corretamente
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_livro_adm, parent, false) // <-- AQUI ESTÁ A CORREÇÃO
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = livros[position]

        holder.titulo.text = livro.titulo ?: "Sem título"
        holder.autor.text = livro.autor ?: "Autor desconhecido"
        holder.isbn.text = "ISBN: ${livro.isbn ?: "N/A"}"

        // MÁGICA DO GLIDE: Baixa o link da internet e joga na ImageView do Card
        if (!livro.capaUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(livro.capaUrl)
                .placeholder(R.drawable.user_placeholder) // Imagem provisória enquanto baixa
                .error(R.drawable.user_placeholder)       // Imagem caso o link quebre
                .into(holder.imgCapa)
        } else {
            holder.imgCapa.setImageResource(R.drawable.user_placeholder) // Caso não tenha link nenhum
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