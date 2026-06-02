package com.example.bibliounifornew.adapter // ✅ Pacote em minúsculo e correto

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
    private val alugueis: List<Aluguel>,
    private val onCancelarClick: (Aluguel, Boolean) -> Unit
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
        val aluguel = alugueis[position]

        holder.textTitulo.text = aluguel.titulo_livro ?: "Sem título"
        holder.textAutor.text = aluguel.autor_livro ?: "Autor desconhecido"

        holder.imagemLivro.load(aluguel.capa_url) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        val stringVencimento = aluguel.data_vencimento ?: ""

        // Lógica inteligente para identificar o modelo "Emprestado" de solicitações
        if (stringVencimento.startsWith("Status:")) {
            val statusReal = stringVencimento.replace("Status:", "").trim()
            val tipoSolicitacao = if (aluguel.dias_restantes == 1L) "PDF / Digital" else "Livro Físico"

            holder.textRotuloVencimento.text = "Tipo de solicitação:"
            holder.textVencimento.text = tipoSolicitacao
            holder.textRotuloDias.text = "Estado da solicitação:"
            holder.textDias.text = statusReal
            holder.btnAcaoStatus.text = "Cancelar Solicitação"
            holder.btnAcaoStatus.setOnClickListener { onCancelarClick(aluguel, true) }
        } else {
            holder.textRotuloVencimento.text = "Validade do aluguel:"
            holder.textVencimento.text = stringVencimento
            holder.textRotuloDias.text = "Dias para o vencimento:"
            holder.textDias.text = "${aluguel.dias_restantes ?: 0} dias"
            holder.btnAcaoStatus.text = "Cancelar Aluguel"
            holder.btnAcaoStatus.setOnClickListener { onCancelarClick(aluguel, false) }
        }
    }

    override fun getItemCount(): Int = alugueis.size
}