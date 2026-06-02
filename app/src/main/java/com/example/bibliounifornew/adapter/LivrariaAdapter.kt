package com.example.bibliounifornew.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.LivrariaItem
import com.google.android.material.button.MaterialButton

class LivrariaAdapter(
    private val itens: List<LivrariaItem>,
    private val onCapaClick: (LivrariaItem) -> Unit,
    private val onRemoverClick: (LivrariaItem) -> Unit
) : RecyclerView.Adapter<LivrariaAdapter.LivrariaViewHolder>() {

    class LivrariaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardCover: CardView = view.findViewById(R.id.cardCoverLivraria)
        val imgCapa: ImageView = view.findViewById(R.id.imgCapaLivraria)
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloLivraria)
        val txtAutor: TextView = view.findViewById(R.id.txtAutorLivraria)
        val txtStatus: TextView = view.findViewById(R.id.txtMarcadorStatus)
        val btnRemover: MaterialButton = view.findViewById(R.id.btnRemoverLivraria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivrariaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_livraria_livro, parent, false)
        return LivrariaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivrariaViewHolder, position: Int) {
        val item = itens[position]
        val context = holder.itemView.context

        holder.txtTitulo.text = item.titulo
        holder.txtAutor.text = item.autor
        holder.imgCapa.load(item.capa_url) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        // 🌟 CORRIGIDO: Agora lendo do arquivo unificado padrão do app
        val sharedPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

        // 🌟 CORRIGIDO: Buscando estritamente pelo ID numérico do livro correspondente às outras telas
        val livroId = item.livro_id ?: 0
        val statusSalvo = sharedPrefs.getString("status_$livroId", "NAO_LIDO")

        // ✨ UX MELHORADA: Atualiza o texto e a cor da TAG dinamicamente conforme o status
        when (statusSalvo) {
            "LENDO" -> {
                holder.txtStatus.text = "Status: Lendo"
                // Laranja suave para indicar processo
                holder.txtStatus.setTextColor(Color.parseColor("#E67E22"))
            }
            "LIDO" -> {
                holder.txtStatus.text = "Status: Lido!"
                // Azul padrão do seu app ou Verde sucesso
                holder.txtStatus.setTextColor(Color.parseColor("#2ECC71"))
            }
            else -> {
                holder.txtStatus.text = "Status: Não Lido"
                // Cinza neutro/escuro para pendente
                holder.txtStatus.setTextColor(Color.parseColor("#7F8C8D"))
            }
        }

        holder.cardCover.setOnClickListener { onCapaClick(item) }
        holder.btnRemover.setOnClickListener { onRemoverClick(item) }
    }

    override fun getItemCount(): Int = itens.size
}