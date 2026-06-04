package com.example.bibliounifornew.usuario

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Livro
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Calendar

class TelaRF14Leitura : Fragment(R.layout.telarf14_leitura) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.telarf14_leitura, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val livro = arguments?.getSerializable("livro") as? Livro

        val imageCapa = view.findViewById<ImageView>(R.id.imageLivroAcoes)
        val textTitulo = view.findViewById<TextView>(R.id.textTituloLivroAcoes)
        val textAutor = view.findViewById<TextView>(R.id.textAutorLivroAcoes)
        val textCategoria = view.findViewById<TextView>(R.id.textCategoriaLivroAcoes)

        if (livro != null) {
            textTitulo.text = livro.titulo
            textAutor.text = livro.autor
            textCategoria.text = livro.categoria ?: "Categoria não informada"

            imageCapa.load(livro.capaUrl) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
        } else {
            Toast.makeText(requireContext(), "Erro ao carregar livro.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        val btnAlugar = view.findViewById<MaterialButton>(R.id.buttonAlugarLivro)
        val btnProcurar = view.findViewById<MaterialButton>(R.id.buttonProcurarLivro)
        val btnAbrirPdf = view.findViewById<MaterialButton>(R.id.buttonAbrirPdfLivro)
        val btnAbrirAudio = view.findViewById<MaterialButton>(R.id.buttonAbrirAudioLivro)
        val btnReservar = view.findViewById<MaterialButton>(R.id.buttonReservarLivro)

        // Botão Alugar
        btnAlugar.setOnClickListener {
            if (livro.disponivel) {
                Toast.makeText(requireContext(), "Livro disponível! Iniciando aluguel...", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Livro indisponível no momento.", Toast.LENGTH_LONG).show()
            }
        }

        // Botão Procurar
        btnProcurar.setOnClickListener {
            requireActivity().finish()
        }

        // Botão PDF
        btnAbrirPdf.setOnClickListener {
            if (!livro.pdfUrl.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Abrindo Google Books...", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(livro.pdfUrl))
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Este livro não possui PDF disponível.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botão Audiobook
        btnAbrirAudio.setOnClickListener {
            Toast.makeText(requireContext(), "Audiobook indisponível no momento.", Toast.LENGTH_SHORT).show()
        }

        // Botão Reservar
        btnReservar.setOnClickListener {
            if (!livro.disponivel) {
                Toast.makeText(requireContext(), "Esse livro já está disponível. Você pode alugá-lo diretamente.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            abrirDialogReserva(livro)
        }
    }

    // Dialog de Reserva
    private fun abrirDialogReserva(livro: Livro) {
        val dialogView = layoutInflater.inflate(R.layout.popup_reserva, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePickerReserva)
        val btnConfirmar = dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarReserva)

        val hoje = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        datePicker.minDate = hoje.timeInMillis

        val limiteMaximo = Calendar.getInstance().apply {
            timeInMillis = hoje.timeInMillis
            add(Calendar.DAY_OF_YEAR, 7)
        }
        datePicker.maxDate = limiteMaximo.timeInMillis

        btnConfirmar.setOnClickListener {
            val dia = datePicker.dayOfMonth
            val mes = datePicker.month + 1
            val ano = datePicker.year

            val dataSelecionada = Calendar.getInstance().apply {
                set(ano, mes - 1, dia, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diferencaMillis = dataSelecionada.timeInMillis - hoje.timeInMillis
            val diferencaDias = (diferencaMillis / (1000 * 60 * 60 * 24)).toInt()

            if (diferencaDias < 0) {
                Toast.makeText(requireContext(), "Escolha uma data válida.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (diferencaDias > 7) {
                Toast.makeText(requireContext(), "A reserva pode ser feita em até 7 dias.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val dataRetirada = "%02d/%02d/%04d".format(dia, mes, ano)
            salvarReserva(livro, dataRetirada)
            dialog.dismiss()
        }
        dialog.show()
    }

    // Salvar Reserva no Supabase
    // Salvar Reserva no Supabase
    private fun salvarReserva(livro: Livro, dataRetirada: String) {
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val emailUsuario = sharedPref.getString("USER_EMAIL", "")?.trim()?.lowercase() ?: ""

        // 🌟 Gera o carimbo de data e hora atual no formato do Supabase
        val timestampAtual = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 1️⃣ Envia os dados exatos para a tabela "reservas"
                    val jsonReserva = buildJsonObject {
                        put("email_usuario", emailUsuario)
                        put("titulo_livro", livro.titulo)
                        put("autor_livro", livro.autor)
                        put("capa_url", livro.capaUrl ?: "")
                        put("data_vencimento", dataRetirada)
                        put("dias_restantes", 7)
                        put("devolvido", false)
                        put("oculto_historico", false)
                        put("data_retirada", dataRetirada)
                        put("created_at", timestampAtual) // 🌟 Enviando o carimbo de data
                    }
                    SupabaseConfig.client.postgrest["reservas"].insert(jsonReserva)

                    // 2️⃣ Salva a notificação do sistema
                    val jsonNotificacao = buildJsonObject {
                        put("email_usuario", emailUsuario)
                        put("titulo", "Reserva Realizada")
                        put("mensagem", "Sua reserva do livro '${livro.titulo}' foi agendada para $dataRetirada.")
                        put("created_at", timestampAtual) // 🌟 Enviando o carimbo de data aqui também
                    }
                    SupabaseConfig.client.postgrest["notificacoes"].insert(jsonNotificacao)
                }

                Toast.makeText(requireContext(), "Reserva realizada com sucesso!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao realizar reserva: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}