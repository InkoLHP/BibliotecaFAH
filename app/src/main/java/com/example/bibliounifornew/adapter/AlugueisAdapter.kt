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
import com.example.bibliounifornew.model.Aluguel
import com.google.android.material.button.MaterialButton

class AluguelAdapter(
    private val alugueis: List<Aluguel>,
    private val onAcaoClick: (Aluguel, Boolean) -> Unit
) : RecyclerView.Adapter<AluguelAdapter.AluguelViewHolder>() {

    class AluguelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagemLivro: ImageView = itemView.findViewById(R.id.imageLivro)
        val textTitulo: TextView = itemView.findViewById(R.id.textTituloLivro)
        val textAutor: TextView = itemView.findViewById(R.id.textAutorLivro)
        val textVencimento: TextView = itemView.findViewById(R.id.textDataVencimento)
        val textDias: TextView = itemView.findViewById(R.id.textDiasRestantes)
        val btnAcaoStatus: MaterialButton = itemView.findViewById(R.id.btnAcaoStatus)
        val textRotuloVencimento: TextView = itemView.findViewById(R.id.textRotuloVencimento)
        val textRotuloDias: TextView = itemView.findViewById(R.id.textRotuloDias)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AluguelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aluguel, parent, false)
        return AluguelViewHolder(view)
    }

    override fun onBindViewHolder(holder: AluguelViewHolder, position: Int) {
        val aluguel = alugueis[position]
        val context = holder.itemView.context

        val ehSolicitacao = aluguel.tipo == "SOLICITACAO" || aluguel.data_vencimento?.contains("Status:") == true
        val ehReserva = aluguel.tipo == "RESERVA"

        holder.textTitulo.text = aluguel.titulo_livro ?: "Sem título"
        holder.textAutor.text = aluguel.autor_livro ?: "Autor desconhecido"

        holder.imagemLivro.load(aluguel.capa_url) {
            crossfade(true)
            placeholder(R.drawable.osda)
            error(R.drawable.osda)
        }

        when {
            ehReserva -> {
                holder.textRotuloVencimento.text = "Data da retirada:"
                holder.textVencimento.text = aluguel.data_retirada ?: ""
                holder.textRotuloDias.text = "Status da reserva:"
                holder.textDias.text = "Aguardando retirada"
                holder.btnAcaoStatus.text = "Cancelar Reserva"
                holder.btnAcaoStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.biblio_red))
                holder.btnAcaoStatus.setOnClickListener { onAcaoClick(aluguel, false) }
            }
            ehSolicitacao -> {
                holder.textRotuloVencimento.text = "Situação da Solicitação:"
                holder.textVencimento.text = aluguel.data_vencimento ?: ""
                holder.textRotuloDias.text = "Tipo de Pedido:"
                holder.textDias.text = if (aluguel.dias_restantes == 1L) "PDF Digital" else "Livro Físico"
                holder.btnAcaoStatus.text = "Cancelar Solicitação"
                holder.btnAcaoStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.biblio_red))
                holder.btnAcaoStatus.setOnClickListener { onAcaoClick(aluguel, true) }
            }
            else -> {
                holder.textRotuloVencimento.text = "Validade do aluguel:"
                holder.textVencimento.text = aluguel.data_vencimento ?: ""
                holder.textRotuloDias.text = "Dias para o vencimento:"
                holder.textDias.text = "${aluguel.dias_restantes ?: 0L} dias"
                holder.btnAcaoStatus.text = "Cancelar Aluguel"
                holder.btnAcaoStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.biblio_blue))
                holder.btnAcaoStatus.setOnClickListener { onAcaoClick(aluguel, false) }
            }
        }
    }

    override fun getItemCount(): Int = alugueis.size
}