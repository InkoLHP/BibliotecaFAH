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
import com.example.bibliounifornew.model.Aluguel
import com.google.android.material.button.MaterialButton

class AluguelADMAdapter(
    private val listaAlugueis: List<Aluguel>,
    private val onVerLivroClick: (Aluguel) -> Unit,
    private val onVerUsuarioClick: (Aluguel) -> Unit
) : RecyclerView.Adapter<AluguelADMAdapter.AluguelViewHolder>() {

    class AluguelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNomeUsuario: TextView = itemView.findViewById(R.id.textNomeUsuarioAluguel)
        val imgCapaLivro: ImageView = itemView.findViewById(R.id.imgCapaLivroAluguel)
        val textTituloLivro: TextView = itemView.findViewById(R.id.textTituloLivroAluguel)
        val textAutorLivro: TextView = itemView.findViewById(R.id.textAutorLivroAluguel)
        val textDataAluguel: TextView = itemView.findViewById(R.id.textDataAluguel)
        val textHoraAluguel: TextView = itemView.findViewById(R.id.textHoraAluguel)
        val textTipoStatus: TextView = itemView.findViewById(R.id.textTipoStatus) // 🌟 NOVO: Mapeado do XML
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

        holder.textNomeUsuario.text = aluguel.email_usuario ?: "Usuário desconhecido"
        holder.textTituloLivro.text = aluguel.titulo_livro ?: "Sem título"
        holder.textAutorLivro.text = aluguel.autor_livro ?: "Autor desconhecido"

        holder.imgCapaLivro.load(aluguel.capa_url) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder) // Garante um fallback caso a URL quebre
        }

        holder.textDataAluguel.text = "Vence: ${aluguel.data_vencimento ?: "Sem data"}"
        holder.textHoraAluguel.visibility = View.GONE

        // 🌟 LÓGICA DE COR E TEXTO (RESERVADO vs ALUGADO)
        if (aluguel.tagTabela == "reservas") {
            holder.textTipoStatus.text = "Reservado:"
            holder.textTipoStatus.setTextColor(Color.parseColor("#F57C00")) // Laranja forte para destaque
        } else {
            holder.textTipoStatus.text = "Alugado:"
            holder.textTipoStatus.setTextColor(Color.BLACK) // Preto padrão para alugados
        }

        // Configurando os botões com os callbacks
        holder.btnVerLivro.setOnClickListener { onVerLivroClick(aluguel) }
        holder.btnVerUsuario.setOnClickListener { onVerUsuarioClick(aluguel) }
    }

    override fun getItemCount(): Int = listaAlugueis.size
}