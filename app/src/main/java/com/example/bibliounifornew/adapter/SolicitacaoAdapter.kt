package com.example.bibliounifornew.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.example.bibliounifornew.model.*

class SolicitacaoAdapter(
    private val lista: MutableList<Solicitacao>,
    private val onVerUsuarioClick: (Solicitacao) -> Unit,
    private val onConcluirSolicitacao: (Solicitacao, Int) -> Unit
) : RecyclerView.Adapter<SolicitacaoAdapter.SolicitacaoViewHolder>() {

    // 🌟 NOVO: Um dicionário (Map) para guardar as fotos vinculadas aos e-mails
    private var mapaFotos: Map<String, String> = emptyMap()

    // 🌟 NOVO: Função para o Fragment injetar as fotos carregadas do banco
    fun atualizarFotos(novoMapa: Map<String, String>) {
        this.mapaFotos = novoMapa
        notifyDataSetChanged()
    }

    inner class SolicitacaoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTituloLivro: TextView = view.findViewById(R.id.textTituloLivro)
        val textAutorLivro: TextView = view.findViewById(R.id.textAutorLivro)
        val textUsuario: TextView = view.findViewById(R.id.textUsuario)
        val textTipoSolicitacao: TextView = view.findViewById(R.id.textTipoSolicitacao)
        val imageCapaLivro: ImageView = view.findViewById(R.id.imageCapaLivro)

        // 🌟 NOVO: Mapeando a foto do usuário do seu XML
        val imageFotoUsuario: ImageView = view.findViewById(R.id.imageFotoUsuario)

        val buttonEnviarPDF: MaterialButton = view.findViewById(R.id.buttonAcaoSolicitacao)
        val buttonExcluirSolicitacao: MaterialButton = view.findViewById(R.id.buttonExcluirSolicitacao)
        val buttonVerSolicitacoesUsuario: MaterialButton = view.findViewById(R.id.buttonVerSolicitacoesUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitacaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitacao, parent, false)
        return SolicitacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitacaoViewHolder, position: Int) {
        val item = lista[position]

        holder.textTituloLivro.text = item.titulo
        holder.textAutorLivro.text = item.autor
        holder.textUsuario.text = item.email_usuario

        val tipoFormatado = when (item.tipo_solicitacao) {
            "LIVRO_FISICO" -> "Livro Físico"
            "PDF_DIGITAL" -> "PDF Digital"
            "AUDIO_BOOK" -> "Audiobook"
            else -> item.tipo_solicitacao
        }
        holder.textTipoSolicitacao.text = tipoFormatado

        // Lógica do botão de ação
        when (item.tipo_solicitacao) {
            "PDF_DIGITAL" -> {
                holder.buttonEnviarPDF.text = "Enviar PDF"
                holder.buttonEnviarPDF.setOnClickListener {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
                    holder.itemView.context.startActivity(Intent.createChooser(intent, "Selecionar PDF"))
                    onConcluirSolicitacao(item, holder.adapterPosition)
                }
            }
            "AUDIO_BOOK" -> {
                holder.buttonEnviarPDF.text = "Enviar Audiobook"
                holder.buttonEnviarPDF.setOnClickListener {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "audio/*" }
                    holder.itemView.context.startActivity(Intent.createChooser(intent, "Selecionar Audiobook"))
                    onConcluirSolicitacao(item, holder.adapterPosition)
                }
            }
            "LIVRO_FISICO" -> {
                holder.buttonEnviarPDF.text = "Confirmar Solicitação"
                holder.buttonEnviarPDF.setOnClickListener {
                    onConcluirSolicitacao(item, holder.adapterPosition)
                }
            }
        }

        // Carrega Capa do Livro
        holder.imageCapaLivro.load(item.capa_url) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        // =========================
        // 🌟 NOVO: Carregar a Foto do Usuário!
        // =========================
        val urlFotoUsuario = mapaFotos[item.email_usuario]
        holder.imageFotoUsuario.load(urlFotoUsuario) {
            crossfade(true)
            placeholder(R.drawable.user_placeholder)
            error(R.drawable.user_placeholder)
        }

        holder.buttonVerSolicitacoesUsuario.setOnClickListener { onVerUsuarioClick(item) }

        holder.buttonExcluirSolicitacao.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Excluir solicitação")
                .setMessage("Deseja realmente excluir esta solicitação?")
                .setPositiveButton("Excluir") { _, _ ->
                    val posicao = holder.adapterPosition
                    if (posicao != RecyclerView.NO_POSITION) {
                        lista.removeAt(posicao)
                        notifyItemRemoved(posicao)
                        Toast.makeText(holder.itemView.context, "Solicitação removida", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount() = lista.size
}