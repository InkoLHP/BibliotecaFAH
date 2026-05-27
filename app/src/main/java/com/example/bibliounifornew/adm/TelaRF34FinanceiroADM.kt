package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.User
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class TelaRF34FinanceiroADM : Fragment(R.layout.telarf34_finaceiro_adm) {

    private val VALOR_MULTA_DIA = 2.00 // R$ 2,00 por dia de atraso

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carregarAlugueisVencidos(view)
    }

    private fun carregarAlugueisVencidos(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.containerLivrosVencidos)
        val inflater = LayoutInflater.from(requireContext())

        container.removeAllViews()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val todosAlugueis = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("alugueis").select().decodeList<Aluguel>()
                }
                val todosUsuarios = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("users").select().decodeList<User>()
                }

                // FILTRO INTELIGENTE: Aceita atraso por data OU por número negativo em dias_restantes
                val alugueisVencidos = todosAlugueis.filter { aluguel ->
                    val atrasadoPorData = calcularDiasAtraso(aluguel.data_vencimento) > 0
                    val atrasadoPorDiasManuais = aluguel.dias_restantes != null && aluguel.dias_restantes < 0

                    !aluguel.devolvido && (atrasadoPorData || atrasadoPorDiasManuais)
                }

                if (alugueisVencidos.isEmpty()) {
                    Toast.makeText(requireContext(), "Nenhum livro em atraso encontrado.", Toast.LENGTH_SHORT).show()
                } else {
                    alugueisVencidos.forEach { aluguel ->
                        val itemView = inflater.inflate(R.layout.item_livro_vencido, container, false)

                        val imgCapa = itemView.findViewById<ImageView>(R.id.imgBookCover)
                        val txtTitulo = itemView.findViewById<TextView>(R.id.txtBookTitle)
                        val txtAutorData = itemView.findViewById<TextView>(R.id.txtBookAuthor)
                        val imgUser = itemView.findViewById<ImageView>(R.id.imgProfileSmall)
                        val txtNomeUser = itemView.findViewById<TextView>(R.id.txtUserName)
                        val txtAtraso = itemView.findViewById<TextView>(R.id.txtDuration)
                        val txtMulta = itemView.findViewById<TextView>(R.id.txtFine)

                        val imgTresPontinhos = itemView.findViewById<ImageView>(R.id.imgMoreOptions)
                        val btnRenovar = itemView.findViewById<MaterialButton>(R.id.btnRenovarAluguel)

                        // Define os dias de atraso priorizando a data, mas usando o número negativo se a data falhar
                        var diasAtraso = calcularDiasAtraso(aluguel.data_vencimento)
                        if (diasAtraso == 0 && aluguel.dias_restantes != null && aluguel.dias_restantes < 0) {
                            diasAtraso = kotlin.math.abs(aluguel.dias_restantes)
                        }

                        val valorMulta = diasAtraso * VALOR_MULTA_DIA
                        val dono = todosUsuarios.find { it.email == aluguel.email_usuario }

                        txtTitulo.text = aluguel.titulo_livro
                        txtAutorData.text = "por ${aluguel.autor_livro}\nRetirada/Vencimento: ${aluguel.data_vencimento}"
                        txtNomeUser.text = dono?.nome ?: aluguel.email_usuario
                        txtAtraso.text = "$diasAtraso Dias"
                        txtMulta.text = "Multa auto: R$ ${String.format("%.2f", valorMulta)}"

                        if (!aluguel.capa_url.isNullOrEmpty()) {
                            Glide.with(requireContext()).load(aluguel.capa_url).into(imgCapa)
                        }

                        imgTresPontinhos.setOnClickListener { processarRemocaoNotificacao(aluguel, view) }
                        btnRenovar.setOnClickListener { abrirPopupRenovacao(aluguel, view) }

                        container.addView(itemView)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar os dados financeiros", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================================
    // MÉTODOS DE MATEMÁTICA DE DATAS
    // ==========================================================

    private fun calcularDiasAtraso(dataVencimentoStr: String?): Int {
        if (dataVencimentoStr.isNullOrEmpty()) return 0
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataVencimento = sdf.parse(dataVencimentoStr) ?: return 0

            // Pega a data de hoje (zerando as horas para não dar erro de fuso horário)
            val calendarHoje = Calendar.getInstance()
            calendarHoje.set(Calendar.HOUR_OF_DAY, 0)
            calendarHoje.set(Calendar.MINUTE, 0)
            calendarHoje.set(Calendar.SECOND, 0)
            calendarHoje.set(Calendar.MILLISECOND, 0)
            val hoje = calendarHoje.time

            // Calcula a diferença em dias
            val diffInMillis = hoje.time - dataVencimento.time
            val diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()

            // Se for positivo, passou da data (está atrasado). Se negativo, ainda está no prazo.
            if (diffInDays > 0) diffInDays else 0
        } catch (e: Exception) {
            0 // Se a data estiver em formato errado no banco, não quebra o app
        }
    }

    private fun formatarDataBR(dataStr: String?): String {
        if (dataStr.isNullOrEmpty()) return ""
        return try {
            val sdfBanco = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfBR = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val data = sdfBanco.parse(dataStr)
            if (data != null) sdfBR.format(data) else dataStr
        } catch (e: Exception) {
            dataStr // Retorna do jeito que veio se não conseguir formatar
        }
    }

    // ==========================================================
    // AÇÕES DE BOTÕES
    // ==========================================================

    private fun processarRemocaoNotificacao(aluguel: Aluguel, telaPrincipalView: View) {
        AlertDialog.Builder(requireContext())
            .setTitle("Resolver Pendência")
            .setMessage("Deseja notificar o usuário ${aluguel.email_usuario} sobre o atraso e remover este registro de multa do sistema?")
            .setPositiveButton("Notificar e Remover") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.from("alugueis").delete { filter { eq("id", aluguel.id!!) } }
                        }
                        Toast.makeText(requireContext(), "Usuário notificado e registro apagado!", Toast.LENGTH_LONG).show()
                        carregarAlugueisVencidos(telaPrincipalView)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Erro ao remover registro", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirPopupRenovacao(aluguel: Aluguel, telaPrincipalView: View) {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "Ex: 7"
        input.setPadding(48, 32, 48, 32)

        AlertDialog.Builder(requireContext())
            .setTitle("Renovar Aluguel")
            .setMessage("Por quantos dias a partir de hoje deseja renovar o livro '${aluguel.titulo_livro}'?")
            .setView(input)
            .setPositiveButton("Renovar") { _, _ ->
                val diasParaRenovar = input.text.toString().toIntOrNull()
                if (diasParaRenovar != null && diasParaRenovar > 0) {
                    executarRenovacao(aluguel, diasParaRenovar, telaPrincipalView)
                } else {
                    Toast.makeText(requireContext(), "Digite um número válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun executarRenovacao(aluguel: Aluguel, novosDias: Int, telaPrincipalView: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Calcula a nova data de vencimento: HOJE + Novos Dias
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, novosDias)
                val novaDataVencimento = sdf.format(calendar.time)

                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("alugueis").update(
                        { set("data_vencimento", novaDataVencimento) }
                    ) { filter { eq("id", aluguel.id!!) } }
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Renovação Confirmada")
                    .setMessage("Aluguel renovado! O novo vencimento é: ${formatarDataBR(novaDataVencimento)}")
                    .setPositiveButton("Voltar") { dialog, _ ->
                        dialog.dismiss()
                        carregarAlugueisVencidos(telaPrincipalView)
                    }
                    .show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao renovar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}