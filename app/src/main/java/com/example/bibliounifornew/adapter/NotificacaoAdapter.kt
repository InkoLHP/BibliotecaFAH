package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Notificacao
import java.text.SimpleDateFormat
import java.util.Locale

class NotificacaoAdapter(
    private val lista: List<Notificacao>
) : RecyclerView.Adapter<NotificacaoAdapter.NotificacaoViewHolder>() {

    inner class NotificacaoViewHolder(view: View)
        : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificacaoViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacoes, parent, false)

        return NotificacaoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: NotificacaoViewHolder,
        position: Int
    ) {

        val item = lista[position]

        holder.itemView.findViewById<TextView>(
            R.id.textTituloNotif
        ).text = item.titulo

        holder.itemView.findViewById<TextView>(
            R.id.textMensagemNotif
        ).text = item.mensagem

        holder.itemView.findViewById<CheckBox>(
            R.id.checkLida
        ).isChecked = item.visualizada

        val horario = holder.itemView.findViewById<TextView>(
            R.id.textDataNotif
        )

        try {

            val entrada = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            )

            val saida = SimpleDateFormat(
                "dd/MM HH:mm",
                Locale.getDefault()
            )

            val data = entrada.parse(
                item.created_at ?: ""
            )

            horario.text = saida.format(data!!)

        } catch (e: Exception) {

            horario.text = "Agora"
        }
    }

    override fun getItemCount() = lista.size
}