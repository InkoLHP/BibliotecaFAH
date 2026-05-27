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
import java.util.*

class NotificacaoAdapter(
    private val listaNotif: List<Notificacao>,
    private val onMarcarComoLida: (Notificacao) -> Unit
) : RecyclerView.Adapter<NotificacaoAdapter.NotifViewHolder>() {

    class NotifViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloItemNotif)
        val txtMensagem: TextView = view.findViewById(R.id.txtMensagemItemNotif)
        val txtTempo: TextView = view.findViewById(R.id.txtTempoItemNotif)
        val checkLida: CheckBox = view.findViewById(R.id.checkLidaItemNotif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacao, parent, false)
        return NotifViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val notificacao = listaNotif[position]

        holder.txtTitulo.text = notificacao.titulo
        holder.txtMensagem.text = notificacao.mensagem
        holder.checkLida.isChecked = notificacao.visualizada
        holder.txtTempo.text = calcularTempoDecorrido(notificacao.created_at)

        // Quando o usuário marcar o CheckBox, dispara a remoção
        holder.checkLida.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onMarcarComoLida(notificacao)
            }
        }
    }

    override fun getItemCount(): Int = listaNotif.size

    // Regra de Negócio: Apresentar quantas horas ou dias se passaram
    private fun calcularTempoDecorrido(dataCriacaoIso: String?): String {
        if (dataCriacaoIso.isNullOrEmpty()) return "Agora"
        return try {
            val formatoIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dataNotif = formatoIso.parse(dataCriacaoIso) ?: return "Agora"
            val agora = Date()

            val diferencaMili = agora.time - dataNotif.time
            val minutos = diferencaMili / (1000 * 60)
            val horas = diferencaMili / (1000 * 60 * 60)
            val dias = diferencaMili / (1000 * 60 * 60 * 24)

            when {
                minutos < 1 -> "Agora mesmo"
                minutos < 60 -> "Há $minutos min"
                horas < 24 -> "Há $horas h"
                else -> "Há $dias dias"
            }
        } catch (e: Exception) {
            "Recentemente"
        }
    }
}