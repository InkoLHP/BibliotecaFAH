package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adapter.AtrasadosAdapter
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.User
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TelaRF34FinanceiroADM : Fragment(R.layout.telarf34_finaceiro_adm) {

    private lateinit var recyclerLivrosVencidos: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, TelaRF28DashboardADM())
                        .commit()
                }
            }
        })

        recyclerLivrosVencidos = view.findViewById(R.id.recyclerLivrosVencidos)
        recyclerLivrosVencidos.layoutManager = LinearLayoutManager(requireContext())

        carregarAlugueisVencidos()
    }

    private fun carregarAlugueisVencidos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {

                val todosAlugueis = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"].select().decodeList<Aluguel>()
                }
                val todosUsuarios = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"].select().decodeList<User>()
                }

                val alugueisVencidos = todosAlugueis.filter { aluguel ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val dataVencimento = try { sdf.parse(aluguel.data_vencimento ?: "") } catch(e: Exception) { null }

                    val hoje = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    val atrasadoPorData = dataVencimento != null && hoje.time - dataVencimento.time > 0
                    val atrasadoPorDiasManuais = aluguel.dias_restantes != null && aluguel.dias_restantes < 0

                    aluguel.devolvido == false && (atrasadoPorData || atrasadoPorDiasManuais)
                }

                if (alugueisVencidos.isEmpty()) {
                    Toast.makeText(requireContext(), "Nenhum livro em atraso encontrado.", Toast.LENGTH_SHORT).show()
                    recyclerLivrosVencidos.adapter = null
                } else {
                    recyclerLivrosVencidos.adapter = AtrasadosAdapter(
                        alugueisVencidos,
                        todosUsuarios,
                        onMoreOptionsClick = { aluguelClicado ->
                            processarRemocaoNotificacao(aluguelClicado)
                        },
                        onRenovarClick = { aluguelClicado ->
                            abrirPopupRenovacao(aluguelClicado)
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar os dados financeiros", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processarRemocaoNotificacao(aluguel: Aluguel) {
        val idSeguro = aluguel.id ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Resolver Pendência")
            .setMessage("Deseja notificar o usuário ${aluguel.email_usuario} sobre o atraso e remover este registro de multa do sistema?")
            .setPositiveButton("Notificar e Remover") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {

                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["alugueis"].delete { filter { eq("id", idSeguro) } }
                        }
                        Toast.makeText(requireContext(), "Usuário notificado e registro apagado!", Toast.LENGTH_LONG).show()
                        carregarAlugueisVencidos()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Erro ao remover registro", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirPopupRenovacao(aluguel: Aluguel) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Ex: 7"
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Renovar Aluguel")
            .setMessage("Por quantos dias a partir de hoje deseja renovar o livro '${aluguel.titulo_livro}'?")
            .setView(input)
            .setPositiveButton("Renovar") { _, _ ->
                val diasParaRenovar = input.text.toString().toIntOrNull()
                if (diasParaRenovar != null && diasParaRenovar > 0) {
                    executarRenovacao(aluguel, diasParaRenovar)
                } else {
                    Toast.makeText(requireContext(), "Digite um número válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun executarRenovacao(aluguel: Aluguel, novosDias: Int) {
        val idSeguro = aluguel.id ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, novosDias)
                val novaDataVencimento = sdf.format(calendar.time)

                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"].update(
                        update = {
                            set("data_vencimento", novaDataVencimento)
                        }
                    ) {
                        filter { eq("id", idSeguro) }
                    }
                }

                val sdfBanco = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfBR = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dataFormatada = try { sdfBR.format(sdfBanco.parse(novaDataVencimento)!!) } catch(e: Exception) { novaDataVencimento }

                AlertDialog.Builder(requireContext())
                    .setTitle("Renovação Confirmada")
                    .setMessage("Aluguel renovado! O novo vencimento é: $dataFormatada")
                    .setPositiveButton("Voltar") { dialog, _ ->
                        dialog.dismiss()
                        carregarAlugueisVencidos()
                    }
                    .show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao renovar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}