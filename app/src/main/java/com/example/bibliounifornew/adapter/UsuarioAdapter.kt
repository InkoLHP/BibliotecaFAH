package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.User

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

        // 🌟 Verifica se o link da foto realmente existe antes de pedir pro Coil carregar
        if (!usuario.foto.isNullOrEmpty()) {
            holder.foto.load(usuario.foto) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
                // Removi o CircleCropTransformation() para evitar conflitos com o seu XML
            }
        } else {
            // 🌟 Se não tiver foto no banco, injeta o placeholder direto!
            holder.foto.setImageResource(R.drawable.user_placeholder)
        }

        // Ações de clique
        holder.itemView.setOnClickListener {
            onUsuarioClick(usuario)
        }

        // Mantém o clique no botão por garantia visual
        holder.btnVer.setOnClickListener {
            onUsuarioClick(usuario)
        }
    }

    override fun getItemCount() = lista.size
}