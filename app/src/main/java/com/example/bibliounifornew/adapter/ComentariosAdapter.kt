package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Avaliacao

class ComentariosAdapter(
    private val comentarios: List<Avaliacao>
) : RecyclerView.Adapter<ComentariosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val usuario: TextView =
            view.findViewById(R.id.textUsuarioComentario)

        val nota: TextView =
            view.findViewById(R.id.textNotaComentario)

        val comentario: TextView =
            view.findViewById(R.id.textComentario)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comentario, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val avaliacao = comentarios[position]

        holder.usuario.text = avaliacao.email ?: "Usuário"

        holder.nota.text = "★ ${avaliacao.nota}"

        holder.comentario.text = avaliacao.comentarios
    }

    override fun getItemCount(): Int {
        return comentarios.size
    }
}