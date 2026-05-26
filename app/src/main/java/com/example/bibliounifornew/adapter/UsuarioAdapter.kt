package com.example.bibliounifornew.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.*

class UsuarioAdapter(
    private val lista: List<User>,
    private val onUsuarioClick: (User) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foto: ImageView = view.findViewById(R.id.imageUsuarioItem)
        val nome: TextView = view.findViewById(R.id.textUsuarioItem)
        val btnVer: ImageView = view.findViewById(R.id.viewVerUsuarioItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario_adm, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = lista[position]
        holder.nome.text = usuario.nome

        Glide.with(holder.itemView.context)
            .load(usuario.foto)
            .placeholder(R.drawable.user_placeholder)
            .into(holder.foto)

        val clique = View.OnClickListener { onUsuarioClick(usuario) }
        holder.nome.setOnClickListener(clique)
        holder.btnVer.setOnClickListener(clique)
    }

    override fun getItemCount() = lista.size
}