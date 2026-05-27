package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Livro
import com.google.android.material.button.MaterialButton

class DescobrirAdapter(
    private val livros: List<Livro>,
    private val onAlugarClick: (Livro) -> Unit,
    private val onCardClick: (Livro) -> Unit
) : RecyclerView.Adapter<DescobrirAdapter.DescobrirViewHolder>() {

    class DescobrirViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCapa: ImageView = view.findViewById(R.id.imgCapaDescobrir)
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloDescobrir)
        val txtAutor: TextView = view.findViewById(R.id.txtAutorDescobrir)
        val btnAlugar: MaterialButton = view.findViewById(R.id.btnAlugarDescobrir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescobrirViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_livro_descobrir, parent, false)
        return DescobrirViewHolder(view)
    }

    override fun onBindViewHolder(holder: DescobrirViewHolder, position: Int) {
        val livro = livros[position]
        holder.txtTitulo.text = livro.titulo
        holder.txtAutor.text = livro.autor
        holder.imgCapa.load(livro.capaUrl) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        holder.itemView.setOnClickListener { onCardClick(livro) }
        holder.btnAlugar.setOnClickListener { onAlugarClick(livro) }
    }

    override fun getItemCount(): Int = livros.size
}