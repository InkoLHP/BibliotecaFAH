package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Aluguel
import com.google.android.material.button.MaterialButton

class AluguelUSERAdapter(
    private val itens: List<Aluguel>,
    private val tipoPadrao: String,
    private val onAcaoClick: (Aluguel, String) -> Unit
) : RecyclerView.Adapter<AluguelUSERAdapter.AluguelViewHolder>() {

    class AluguelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagemLivro: ImageView = itemView.findViewById(R.id.imageLivro)
        val textTitulo: TextView = itemView.findViewById(R.id.textTituloLivro)
        val textAutor: TextView = itemView.findViewById(R.id.textAutorLivro)
        val textVencimento: TextView = itemView.findViewById(R.id.textDataVencimento)
        val textDias: TextView = itemView.findViewById(R.id.textDiasRestantes)
        val textRotuloVencimento: TextView = itemView.findViewById(R.id.textRotuloVencimento)
        val textRotuloDias: TextView = itemView.findViewById(R.id.textRotuloDias)
        val btnAcaoStatus: MaterialButton = itemView.findViewById(R.id.btnAcaoStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AluguelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aluguel, parent, false)
        return AluguelViewHolder(view)
    }

    override fun onBindViewHolder(holder: AluguelViewHolder, position: Int) {
        val item = itens[position]

        holder.textTitulo.text = item.titulo_livro ?: "Sem título"
        holder.textAutor.text = item.autor_livro ?: "Autor desconhecido"

        holder.imagemLivro.load(item.capa_url) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        // Baseia-se na tag individual injetada durante a busca no Supabase
        when (item.tagTabela.lowercase()) {
            "alugueis" -> {
                holder.textRotuloVencimento.text = "Validade do aluguel:"
                holder.textVencimento.text = item.data_vencimento ?: "Não definida"
                holder.textRotuloDias.text = "Dias restantes:"
                holder.textDias.text = "${item.dias_restantes ?: 0} dias"
                holder.btnAcaoStatus.text = "Cancelar Aluguel"
                holder.btnAcaoStatus.visibility = View.VISIBLE
            }
            "solicitacoes" -> {
                holder.textRotuloVencimento.text = "Status da Solicitação:"
                holder.textVencimento.text = "Aguardando Aprovação"
                holder.textRotuloDias.text = "Data do Pedido:"
                holder.textDias.text = item.data_vencimento ?: "Recentemente"
                holder.btnAcaoStatus.text = "Cancelar Solicitação"
                holder.btnAcaoStatus.visibility = View.VISIBLE
            }
            "reservas" -> {
                holder.textRotuloVencimento.text = "Disponível para retirada até:"
                holder.textVencimento.text = item.data_vencimento ?: "Verifique no balcão"
                holder.textRotuloDias.text = "Data Limite:"
                holder.textDias.text = "${item.dias_restantes ?: 0} dias úteis"
                holder.btnAcaoStatus.text = "Cancelar Reserva"
                holder.btnAcaoStatus.visibility = View.VISIBLE
            }
            else -> {
                holder.btnAcaoStatus.visibility = View.GONE
            }
        }

        holder.btnAcaoStatus.setOnClickListener {
            onAcaoClick(item, item.tagTabela)
        }
    }

    override fun getItemCount(): Int = itens.size
}