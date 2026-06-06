package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Notificacao
import com.google.android.material.button.MaterialButton

class NotificacaoAdapter(
    private val listaNotificacoes: List<Notificacao>,
    private val onAvisoLido: (Notificacao) -> Unit,
    private val onAceitarConvite: (Notificacao) -> Unit,
    private val onRecusarConvite: (Notificacao) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TIPO_AVISO = 0
        private const val TIPO_CONVITE = 1
    }

    // 🌟 Verifica o tipo para decidir qual layout usar
    override fun getItemViewType(position: Int): Int {
        return if (listaNotificacoes[position].tipo == "convite_amizade") {
            TIPO_CONVITE
        } else {
            TIPO_AVISO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TIPO_CONVITE) {
            // Usa o layout novo que criamos para o pedido de amizade
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacao_amizade, parent, false)
            ConviteViewHolder(view)
        } else {
            // Usa o layout padrão de notificação (substitua o nome se o seu for diferente)
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacao, parent, false)
            AvisoViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notificacao = listaNotificacoes[position]

        if (holder is ConviteViewHolder) {
            holder.bind(notificacao, onAceitarConvite, onRecusarConvite)
        } else if (holder is AvisoViewHolder) {
            holder.bind(notificacao, onAvisoLido)
        }
    }

    override fun getItemCount(): Int = listaNotificacoes.size

    // ViewHolder para o Aviso Comum
    class AvisoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTitulo = itemView.findViewById<TextView>(R.id.txtTituloItemNotif)
        private val txtMensagem = itemView.findViewById<TextView>(R.id.txtMensagemItemNotif)
        private val checkLida = itemView.findViewById<CheckBox>(R.id.checkLidaItemNotif)

        fun bind(notificacao: Notificacao, onLido: (Notificacao) -> Unit) {
            txtTitulo.text = notificacao.titulo
            txtMensagem.text = notificacao.mensagem

            checkLida.setOnCheckedChangeListener(null)
            checkLida.isChecked = notificacao.visualizada

            checkLida.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) onLido(notificacao)
            }
        }
    }

    // ViewHolder para o Convite de Amizade
    class ConviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTitulo = itemView.findViewById<TextView>(R.id.textTituloConvite)
        private val txtMensagem = itemView.findViewById<TextView>(R.id.textMensagemConvite)
        private val btnAceitar = itemView.findViewById<MaterialButton>(R.id.buttonAceitarAmigo)
        private val btnRecusar = itemView.findViewById<MaterialButton>(R.id.buttonRecusarAmigo)

        fun bind(notificacao: Notificacao, onAceitar: (Notificacao) -> Unit, onRecusar: (Notificacao) -> Unit) {
            txtTitulo.text = notificacao.titulo
            txtMensagem.text = notificacao.mensagem

            btnAceitar.setOnClickListener { onAceitar(notificacao) }
            btnRecusar.setOnClickListener { onRecusar(notificacao) }
        }
    }
}