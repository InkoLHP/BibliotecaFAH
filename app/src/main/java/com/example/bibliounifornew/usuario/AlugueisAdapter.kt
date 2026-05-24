package com.example.bibliounifornew.usuario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R

// Adapter do Recicler View, pega a lista de alugueis e preenche os cards automaticamente.
class AluguelAdapter(
    private val alugueis: List<Aluguel>
) : RecyclerView.Adapter<AluguelAdapter.AluguelViewHolder>() {

    class AluguelViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        val imagemLivro: ImageView =
            itemView.findViewById(R.id.imageLivro)

        val textTitulo: TextView =
            itemView.findViewById(R.id.textTituloLivro)

        val textAutor: TextView =
            itemView.findViewById(R.id.textAutorLivro)

        val textVencimento: TextView =
            itemView.findViewById(R.id.textDataVencimento)

        val textDias: TextView =
            itemView.findViewById(R.id.textDiasRestantes)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AluguelViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aluguel, parent, false)

        return AluguelViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AluguelViewHolder,
        position: Int
    ) {

        val aluguel = alugueis[position]

        holder.textTitulo.text =
            aluguel.titulo_livro ?: "Sem título"

        holder.textAutor.text =
            aluguel.autor_livro ?: "Autor desconhecido"

        holder.textVencimento.text =
            aluguel.data_vencimento ?: "Sem data"

        holder.textDias.text =
            "${aluguel.dias_restantes ?: 0} dias"

        holder.imagemLivro.load(aluguel.capa_url) {

            crossfade(true)

            placeholder(R.drawable.osda)

            error(R.drawable.osda)
        }
    }

    override fun getItemCount(): Int {

        return alugueis.size
    }
}