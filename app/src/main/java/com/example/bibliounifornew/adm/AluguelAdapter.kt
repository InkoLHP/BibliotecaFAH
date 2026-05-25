package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.Aluguel
import com.google.android.material.button.MaterialButton

class AluguelAdapter(
    private val listaAlugueis: List<Aluguel>,
    private val onVerLivroClick: (Aluguel) -> Unit,
    private val onVerUsuarioClick: (Aluguel) -> Unit
) : RecyclerView.Adapter<AluguelAdapter.AluguelViewHolder>() {

    class AluguelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNomeUsuario: TextView = itemView.findViewById(R.id.textNomeUsuarioAluguel)
        val imgCapaLivro: ImageView = itemView.findViewById(R.id.imgCapaLivroAluguel)
        val textTituloLivro: TextView = itemView.findViewById(R.id.textTituloLivroAluguel)
        val textAutorLivro: TextView = itemView.findViewById(R.id.textAutorLivroAluguel)
        val textDataAluguel: TextView = itemView.findViewById(R.id.textDataAluguel)
        val textHoraAluguel: TextView = itemView.findViewById(R.id.textHoraAluguel)
        val btnVerLivro: MaterialButton = itemView.findViewById(R.id.btnVerLivro)
        val btnVerUsuario: MaterialButton = itemView.findViewById(R.id.btnVerUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AluguelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aluguel_adm, parent, false)
        return AluguelViewHolder(view)
    }

    override fun onBindViewHolder(holder: AluguelViewHolder, position: Int) {
        val aluguel = listaAlugueis[position]

        holder.textNomeUsuario.text = aluguel.emailUsuario ?: "Usuário desconhecido"
        holder.textTituloLivro.text = aluguel.tituloLivro ?: "Sem título"
        holder.textAutorLivro.text = aluguel.autorLivro ?: "Autor desconhecido"

        // Usa o Glide para carregar a capa pela URL que está no banco
        Glide.with(holder.itemView.context)
            .load(aluguel.capaUrl)
            .placeholder(R.drawable.o_alienista_capa)
            .into(holder.imgCapaLivro)

        // Quebrando o "created_at" (2026-05-24 23:40:09...) para exibir data e hora separados
        val dataHora = aluguel.createdAt?.split("T", " ")
        if (dataHora != null && dataHora.size >= 2) {
            holder.textDataAluguel.text = dataHora[0]
            holder.textHoraAluguel.text = dataHora[1].substring(0, 5) // Pega só o HH:mm
        } else {
            holder.textDataAluguel.text = "Sem data"
            holder.textHoraAluguel.visibility = View.GONE
        }

        // Configurando os botões
        holder.btnVerLivro.setOnClickListener { onVerLivroClick(aluguel) }
        holder.btnVerUsuario.setOnClickListener { onVerUsuarioClick(aluguel) }
    }

    override fun getItemCount(): Int = listaAlugueis.size
}