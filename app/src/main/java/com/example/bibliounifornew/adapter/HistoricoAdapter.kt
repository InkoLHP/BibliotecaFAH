package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.example.bibliounifornew.model.Aluguel

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

        val vencimentoSeguro = aluguel.data_vencimento ?: ""

        if (vencimentoSeguro.startsWith("Status:")) {
            val statusReal = vencimentoSeguro.replace("Status:", "").trim()
            holder.txtStatus.text = "Solicitação: $statusReal"
            holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.biblio_red))
        } else {
            holder.txtStatus.text = "Alugado por: ${aluguel.email_usuario} | Vence em: $vencimentoSeguro"
            holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.biblio_blue))
        }

        // 🚀 Padronizado: Agora usa Coil em vez de Glide
        holder.imgCapa.load(aluguel.capa_url) {
            crossfade(true)
            placeholder(R.drawable.osda)
            error(R.drawable.osda)
        }

        holder.btnRemover.setOnClickListener {
            onRemoverClick(aluguel)
        }
    }

    override fun getItemCount(): Int = listaAlugueis.size
}
