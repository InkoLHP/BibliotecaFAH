package com.example.bibliounifornew.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.example.bibliounifornew.model.*

class HistoricoAdapter(
    private val listaAlugueis: List<Aluguel>,
    private val onRemoverClick: (Aluguel) -> Unit
) : RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder>() {

    class HistoricoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCapa: ImageView = view.findViewById(R.id.imgCapaHistorico)
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloHistorico)
        val txtAutor: TextView = view.findViewById(R.id.txtAutorHistorico)
        val txtStatus: TextView = view.findViewById(R.id.txtStatusHistorico)
        val btnRemover: MaterialButton = view.findViewById(R.id.btnRemoverHistorico)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico_aluguel, parent, false)
        return HistoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        val aluguel = listaAlugueis[position]
        val context = holder.itemView.context

        holder.txtTitulo.text = aluguel.titulo_livro
        holder.txtAutor.text = aluguel.autor_livro

        if (aluguel.data_vencimento.startsWith("Status:")) {
            // É UMA SOLICITAÇÃO!
            val statusReal = aluguel.data_vencimento.replace("Status:", "").trim()

            holder.txtStatus.text = "Solicitação: $statusReal"
            // Puxa a cor vermelha/alerta do seu arquivo colors.xml de forma segura
            holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.biblio_red))
        } else {
            // É UM ALUGUEL REAL!
            holder.txtStatus.text = "Alugado por: ${aluguel.email_usuario} | Vence em: ${aluguel.data_vencimento}"
            // Puxa a cor azul padrão do seu arquivo colors.xml
            holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.biblio_blue))
        }

        // Carrega a capa do livro usando Glide
        if (!aluguel.capa_url.isNullOrEmpty()) {
            Glide.with(context)
                .load(aluguel.capa_url)
                .placeholder(R.drawable.placeholder)
                .into(holder.imgCapa)
        }

        holder.btnRemover.setOnClickListener {
            onRemoverClick(aluguel)
        }
    }

    override fun getItemCount(): Int = listaAlugueis.size
}