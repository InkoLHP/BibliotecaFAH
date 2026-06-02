package com.example.bibliounifornew.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.User
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class AtrasadosAdapter(
    private val listaAlugueis: List<Aluguel>,
    private val listaUsuarios: List<User>,
    private val onMoreOptionsClick: (Aluguel) -> Unit,
    private val onRenovarClick: (Aluguel) -> Unit
) : RecyclerView.Adapter<AtrasadosAdapter.AtrasadosViewHolder>() {

    private val VALOR_MULTA_DIA = 2.00

    class AtrasadosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCapa: ImageView = view.findViewById(R.id.imgBookCover)
        val txtTitulo: TextView = view.findViewById(R.id.txtBookTitle)
        val txtAutorData: TextView = view.findViewById(R.id.txtBookAuthor)
        val imgUser: ImageView = view.findViewById(R.id.imgProfileSmall)
        val txtNomeUser: TextView = view.findViewById(R.id.txtUserName)
        val txtAtraso: TextView = view.findViewById(R.id.txtDuration)
        val txtMulta: TextView = view.findViewById(R.id.txtFine)
        val imgMoreOptions: ImageView = view.findViewById(R.id.imgMoreOptions)
        val btnRenovar: MaterialButton = view.findViewById(R.id.btnRenovarAluguel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AtrasadosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_livro_vencido, parent, false)
        return AtrasadosViewHolder(view)
    }

    override fun onBindViewHolder(holder: AtrasadosViewHolder, position: Int) {
        val aluguel = listaAlugueis[position]

        val dono = listaUsuarios.find { it.email == aluguel.email_usuario }

        var diasAtraso = calcularDiasAtraso(aluguel.data_vencimento)
        if (diasAtraso == 0 && aluguel.dias_restantes != null && aluguel.dias_restantes < 0) {
            diasAtraso = kotlin.math.abs(aluguel.dias_restantes).toInt()
        }

        val valorMulta = diasAtraso * VALOR_MULTA_DIA

        holder.txtTitulo.text = aluguel.titulo_livro
        holder.txtAutorData.text = "por ${aluguel.autor_livro}\nRetirada/Vencimento: ${aluguel.data_vencimento}"
        holder.txtNomeUser.text = dono?.nome ?: aluguel.email_usuario
        holder.txtAtraso.text = "$diasAtraso Dias"
        holder.txtMulta.text = "Multa auto: R$ ${String.format("%.2f", valorMulta)}"

        holder.imgCapa.load(aluguel.capa_url) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        holder.imgUser.load(dono?.foto) {
            crossfade(true)
            placeholder(R.drawable.user_placeholder)
            error(R.drawable.user_placeholder)
            transformations(CircleCropTransformation())
        }

        holder.imgMoreOptions.setOnClickListener { onMoreOptionsClick(aluguel) }
        holder.btnRenovar.setOnClickListener { onRenovarClick(aluguel) }
    }

    override fun getItemCount() = listaAlugueis.size

    private fun calcularDiasAtraso(dataVencimentoStr: String?): Int {
        if (dataVencimentoStr.isNullOrEmpty()) return 0
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataVencimento = sdf.parse(dataVencimentoStr) ?: return 0

            val calendarHoje = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val hoje = calendarHoje.time

            val diffInMillis = hoje.time - dataVencimento.time
            val diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()

            if (diffInDays > 0) diffInDays else 0
        } catch (e: Exception) {
            0
        }
    }
}