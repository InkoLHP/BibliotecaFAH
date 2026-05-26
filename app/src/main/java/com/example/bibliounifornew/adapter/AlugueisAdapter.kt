package com.example.bibliounifornew.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.example.bibliounifornew.model.*

class AluguelAdapter(
    private val alugueis: List<Aluguel>,
    private val onCancelarClick: (Aluguel, Boolean) -> Unit // 🌟 Callback: retorna o item e um Booleano (true se for solicitação, false se for aluguel)
) : RecyclerView.Adapter<AluguelAdapter.AluguelViewHolder>() {

    class AluguelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagemLivro: ImageView = itemView.findViewById(R.id.imageLivro)
        val textTitulo: TextView = itemView.findViewById(R.id.textTituloLivro)
        val textAutor: TextView = itemView.findViewById(R.id.textAutorLivro)
        val textVencimento: TextView = itemView.findViewById(R.id.textDataVencimento)
        val textDias: TextView = itemView.findViewById(R.id.textDiasRestantes)

        // Mapeamento dos novos componentes do XML
        val textRotuloVencimento: TextView = itemView.findViewById(R.id.textRotuloVencimento)
        val textRotuloDias: TextView = itemView.findViewById(R.id.textRotuloDias)
        val btnAcaoStatus: MaterialButton = itemView.findViewById(R.id.btnAcaoStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AluguelViewHolder {
        // Certifique-se de que o nome aqui corresponde ao arquivo XML que você atualizou
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aluguel, parent, false)
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

        // 🌟 LÓGICA DINÂMICA: Verifica se o item envelopado é uma Solicitação ou um Aluguel ativo
        val stringVencimento = aluguel.data_vencimento ?: ""

        if (stringVencimento.startsWith("Status:")) {
            // É UMA SOLICITAÇÃO!
            val statusReal = stringVencimento.replace("Status:", "").trim()
            val tipoSolicitacao = if (aluguel.dias_restantes == 1) "PDF / Digital" else "Livro Físico"

            // Adapta os rótulos fixos do XML para o contexto de Solicitação
            holder.textRotuloVencimento.text = "Tipo de solicitação:"
            holder.textVencimento.text = tipoSolicitacao

            holder.textRotuloDias.text = "Estado da solicitação:"
            holder.textDias.text = statusReal

            // Ajusta o botão
            holder.btnAcaoStatus.text = "Cancelar Solicitação"

            holder.btnAcaoStatus.setOnClickListener {
                onCancelarClick(aluguel, true) // Passa true avisando que é solicitação
            }
        } else {
            // É UM ALUGUEL REAL ATIVO!
            holder.textRotuloVencimento.text = "Validade do aluguel:"
            holder.textVencimento.text = stringVencimento

            holder.textRotuloDias.text = "Dias para o vencimento:"
            holder.textDias.text = "${aluguel.dias_restantes ?: 0} dias"

            // Ajusta o botão
            holder.btnAcaoStatus.text = "Cancelar Aluguel"

            holder.btnAcaoStatus.setOnClickListener {
                onCancelarClick(aluguel, false) // Passa false avisando que é aluguel comum
            }
        }
    }

    override fun getItemCount(): Int = alugueis.size
}