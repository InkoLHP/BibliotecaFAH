package com.example.bibliounifornew.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adm.TelaRF37EditarMidia
import com.example.bibliounifornew.model.Midia
import com.google.android.material.button.MaterialButton

class MidiaAdminAdapter(
    private val listaMidias: List<Midia>
) : RecyclerView.Adapter<MidiaAdminAdapter.MidiaViewHolder>() {

    class MidiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTituloLivro: TextView = itemView.findViewById(R.id.textTituloLivro)
        val textAutorLivro: TextView = itemView.findViewById(R.id.textAutorLivro)
        val textIsbnLivro: TextView = itemView.findViewById(R.id.textIsbnLivro)
        val imgCapaLivro: ImageView = itemView.findViewById(R.id.imgCapaLivro)
        val btnEditarInformacoes: MaterialButton = itemView.findViewById(R.id.btnEditarInformacoes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MidiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_livro_adm, parent, false)
        return MidiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MidiaViewHolder, position: Int) {
        val midia = listaMidias[position]

        holder.textTituloLivro.text = midia.titulo
        holder.textAutorLivro.text = midia.autor
        holder.textIsbnLivro.text = "ISBN: ${midia.isbn}"

        // Carrega a imagem da mídia usando Coil
        holder.imgCapaLivro.load(midia.capaUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        holder.btnEditarInformacoes.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TelaRF37EditarMidia::class.java).apply {
                putExtra("LIVRO_ID", midia.id.toString())
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaMidias.size
}
