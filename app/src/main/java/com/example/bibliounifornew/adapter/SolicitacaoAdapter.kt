package com.example.bibliounifornew.adapter

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adm.Telarf30UsuarioAlugadosADM
import com.google.android.material.button.MaterialButton
import com.example.bibliounifornew.model.*

class SolicitacaoAdapter(
    private val lista: MutableList<Solicitacao>,
    private val onConcluirSolicitacao: (Solicitacao, Int) -> Unit
) : RecyclerView.Adapter<SolicitacaoAdapter.SolicitacaoViewHolder>() {

    inner class SolicitacaoViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val textTituloLivro: TextView =
            view.findViewById(R.id.textTituloLivro)

        val textAutorLivro: TextView =
            view.findViewById(R.id.textAutorLivro)

        val textUsuario: TextView =
            view.findViewById(R.id.textUsuario)

        val textTipoSolicitacao: TextView =
            view.findViewById(R.id.textTipoSolicitacao)

        val imageCapaLivro: ImageView =
            view.findViewById(R.id.imageCapaLivro)

        val buttonEnviarPDF: MaterialButton =
            view.findViewById(R.id.buttonAcaoSolicitacao)

        val buttonExcluirSolicitacao: MaterialButton =
            view.findViewById(R.id.buttonExcluirSolicitacao)

        val buttonVerSolicitacoesUsuario: MaterialButton =
            view.findViewById(R.id.buttonVerSolicitacoesUsuario)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SolicitacaoViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_solicitacao, parent, false)

        return SolicitacaoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SolicitacaoViewHolder,
        position: Int
    ) {

        val item = lista[position]

        // TEXTO
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

        when (item.tipo_solicitacao) {

            "PDF_DIGITAL" -> {
                holder.buttonEnviarPDF.text = "Enviar PDF"

                holder.buttonEnviarPDF.setOnClickListener {

                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "application/pdf"

                    holder.itemView.context.startActivity(
                        Intent.createChooser(intent, "Selecionar PDF")
                    )

                    onConcluirSolicitacao(item, holder.adapterPosition)
                }

            }

            "AUDIO_BOOK" -> {
                holder.buttonEnviarPDF.text = "Enviar Audiobook"

                holder.buttonEnviarPDF.setOnClickListener {

                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "audio/*"

                    holder.itemView.context.startActivity(
                        Intent.createChooser(intent, "Selecionar Audiobook")
                    )

                    onConcluirSolicitacao(item, holder.adapterPosition)
                }
            }

            "LIVRO_FISICO" -> {
                holder.buttonEnviarPDF.text = "Confirmar Solicitação"

                holder.buttonEnviarPDF.setOnClickListener {

                    onConcluirSolicitacao(
                        item,
                        holder.adapterPosition
                    )
                }
            }
        }

        holder.imageCapaLivro.load(item.capa_url) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }




        // =========================
        // VER SOLICITAÇÕES
        // =========================

        holder.buttonVerSolicitacoesUsuario.setOnClickListener {
            val contexto = holder.itemView.context

            // Cria o fragmento de destino
            val fragment = Telarf30UsuarioAlugadosADM().apply {
                arguments = Bundle().apply {
                    putString("email", item.email_usuario)
                    putString("nome", item.nome_usuario)
                    putString("foto", item.foto_usuario)
                    putBoolean("apenasAtrasos", false)
                }
            }

            // Gerencia a transição de telas (Fragments)
            if (contexto is androidx.fragment.app.FragmentActivity) {
                contexto.supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragment) // Certifique-se de que o ID do container principal do seu app é frameLayout
                    .addToBackStack(null)
                    .commit()
            }
        }

        // =========================
        // EXCLUIR
        // =========================

        holder.buttonExcluirSolicitacao.setOnClickListener {

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Excluir solicitação")
                .setMessage(
                    "Deseja realmente excluir esta solicitação?"
                )

                .setPositiveButton("Excluir") { _, _ ->

                    val posicao = holder.adapterPosition

                    if (posicao != RecyclerView.NO_POSITION) {

                        lista.removeAt(posicao)

                        notifyItemRemoved(posicao)

                        Toast.makeText(
                            holder.itemView.context,
                            "Solicitação removida",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount() = lista.size
}