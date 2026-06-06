package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.UsuarioItem
import com.google.android.material.card.MaterialCardView

class UsuarioBuscaAdapter(
    private val usuarios: List<UsuarioItem>,
    private val meusAmigosEmails: List<String>, // 🌟 NOVA LISTA DE E-MAILS DOS AMIGOS AQUI
    private val onCardClick: (UsuarioItem) -> Unit,
    private val onAdicionarClick: (UsuarioItem) -> Unit
) : RecyclerView.Adapter<UsuarioBuscaAdapter.BuscaViewHolder>() {

    class BuscaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageUsuario: ImageView = view.findViewById(R.id.imageUsuarioItem)
        val textNome: TextView = view.findViewById(R.id.textUsuarioItem)
        val btnAdicionar: MaterialCardView = view.findViewById(R.id.btnAdicionarAmigoItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuscaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario_amigo, parent, false)
        return BuscaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuscaViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.textNome.text = usuario.nome ?: "Usuário comum"

        if (!usuario.foto.isNullOrEmpty()) {
            holder.imageUsuario.load(usuario.foto) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        } else {
            holder.imageUsuario.setImageResource(R.drawable.user_placeholder)
        }

        // 🌟 LÓGICA DO BOTÃO: Verifica se já é amigo
        if (meusAmigosEmails.contains(usuario.email)) {
            holder.btnAdicionar.visibility = View.GONE // Esconde o botão "+" da lista
        } else {
            holder.btnAdicionar.visibility = View.VISIBLE // Mostra o botão para quem não é amigo
        }

        // Ações de clique
        holder.itemView.setOnClickListener { onCardClick(usuario) }
        holder.btnAdicionar.setOnClickListener { onAdicionarClick(usuario) }
    }

    override fun getItemCount() = usuarios.size
}