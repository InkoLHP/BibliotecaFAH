package com.example.bibliounifornew.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.DesejoItem
import com.google.android.material.button.MaterialButton

class DesejosAdapter(
    private val itens: List<DesejoItem>,
    private val onCapaClick: (DesejoItem) -> Unit,
    private val onRemoverClick: (DesejoItem) -> Unit,
    private val onLivrariaClick: (DesejoItem) -> Unit,
    private val onAlugarClick: (DesejoItem) -> Unit
) : RecyclerView.Adapter<DesejosAdapter.DesejoViewHolder>() {

    class DesejoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapa: ImageView = itemView.findViewById(R.id.imgCapaDesejo)
        val txtSeloIndisponivel: TextView = itemView.findViewById(R.id.txtSeloIndisponivel)
        val txtTitulo: TextView = itemView.findViewById(R.id.txtTituloDesejo)
        val txtAutor: TextView = itemView.findViewById(R.id.txtAutorDesejo)
        val txtStatusEstoque: TextView = itemView.findViewById(R.id.txtStatusEstoque)
        val btnRemover: MaterialButton = itemView.findViewById(R.id.btnRemoverDesejo)
        val btnLivraria: MaterialButton = itemView.findViewById(R.id.btnLivrariaDesejo)
        val btnAlugar: MaterialButton = itemView.findViewById(R.id.btnAlugarDesejo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DesejoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_livro_desejo, parent, false)
        return DesejoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DesejoViewHolder, position: Int) {
        val item = itens[position]

        holder.txtTitulo.text = item.titulo
        holder.txtAutor.text = item.autor

        // 🚀 Padronizado: Agora usa Coil em vez de Glide
        holder.imgCapa.load(item.capa_url) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        val estaDisponivel = item.disponivel ?: true

        if (estaDisponivel) {
            holder.imgCapa.alpha = 1.0f
            holder.txtSeloIndisponivel.visibility = View.GONE
            holder.txtStatusEstoque.text = "Disponível para Aluguel ✅"
            holder.txtStatusEstoque.setTextColor(Color.parseColor("#2E7D32"))
            holder.btnAlugar.isEnabled = true
            holder.btnAlugar.alpha = 1.0f
        } else {
            holder.imgCapa.alpha = 0.4f
            holder.txtSeloIndisponivel.visibility = View.VISIBLE
            holder.txtStatusEstoque.text = "Livro fora de estoque ❌"
            holder.txtStatusEstoque.setTextColor(Color.parseColor("#C62828"))
            holder.btnAlugar.isEnabled = false
            holder.btnAlugar.alpha = 0.5f 
        }

        holder.imgCapa.setOnClickListener { onCapaClick(item) }
        holder.btnRemover.setOnClickListener { onRemoverClick(item) }
        holder.btnLivraria.setOnClickListener { onLivrariaClick(item) }
        holder.btnAlugar.setOnClickListener { onAlugarClick(item) }
    }

    override fun getItemCount(): Int = itens.size
}
