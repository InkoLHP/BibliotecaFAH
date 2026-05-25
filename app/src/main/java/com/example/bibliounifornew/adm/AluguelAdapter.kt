package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.example.bibliounifornew.usuario.Aluguel // Importado da pasta usuario
import com.google.android.material.button.MaterialButton

class AluguelAdapter(
    private val listaAlugueis: List<Aluguel>,
    private val onVerLivroClick: (Aluguel) -> Unit,
    private val onVerUsuarioClick: (Aluguel) -> Unit
) : RecyclerView.Adapter<AluguelAdapter.AluguelViewHolder>() {

    class AluguelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNomeUsuario: TextView = itemView.findViewById(R.id.textNomeUsuarioAluguel)
        val imgCapaLivro: ImageView = itemView.findViewById(R.id.imgCapaLivroAluguel)
        val textTituloLivro: TextView = itemView.findViewById(R.id.textTituloLivroAluguel)
        val textAutorLivro: TextView = itemView.findViewById(R.id.textAutorLivroAluguel)
        val textDataAluguel: TextView = itemView.findViewById(R.id.textDataAluguel)
        val textHoraAluguel: TextView = itemView.findViewById(R.id.textHoraAluguel)
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

        // Ajustado para os nomes de variáveis do seu Aluguel original
        holder.textNomeUsuario.text = aluguel.email_usuario ?: "Usuário desconhecido"
        holder.textTituloLivro.text = aluguel.titulo_livro ?: "Sem título"
        holder.textAutorLivro.text = aluguel.autor_livro ?: "Autor desconhecido"

        // Usa o Glide para carregar a capa pela URL que está no banco
        Glide.with(holder.itemView.context)
            .load(aluguel.capa_url)
            .placeholder(R.drawable.o_alienista_capa)
            .into(holder.imgCapaLivro)

        // Como o modelo do usuário não possui created_at, exibimos a data de vencimento no card
        holder.textDataAluguel.text = "Vence: ${aluguel.data_vencimento ?: "Sem data"}"
        holder.textHoraAluguel.visibility = View.GONE

        // Configurando os botões
        holder.btnVerLivro.setOnClickListener { onVerLivroClick(aluguel) }
        holder.btnVerUsuario.setOnClickListener { onVerUsuarioClick(aluguel) }
    }

    override fun getItemCount(): Int = listaAlugueis.size
}