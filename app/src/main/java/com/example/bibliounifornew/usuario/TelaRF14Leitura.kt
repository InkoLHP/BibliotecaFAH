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
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.Livro
import com.example.bibliounifornew.model.Notificacao
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                placeholder(R.drawable.o_alienista_capa)
                error(R.drawable.o_alienista_capa)
            }

        } else {

            Toast.makeText(
                requireContext(),
                "Erro ao carregar livro.",
                Toast.LENGTH_SHORT
            ).show()

            parentFragmentManager.popBackStack()

            return
        }

        val btnAlugar =
            view.findViewById<MaterialButton>(R.id.buttonAlugarLivro)

        val btnProcurar =
            view.findViewById<MaterialButton>(R.id.buttonProcurarLivro)

        val btnAbrirPdf =
            view.findViewById<MaterialButton>(R.id.buttonAbrirPdfLivro)

        val btnAbrirAudio =
            view.findViewById<MaterialButton>(R.id.buttonAbrirAudioLivro)

        val btnReservar =
            view.findViewById<MaterialButton>(R.id.buttonReservarLivro)

        // =========================================================
        // BOTÃO ALUGAR
        // =========================================================

        btnAlugar.setOnClickListener {

            if (livro.disponivel) {

                Toast.makeText(
                    requireContext(),
                    "Livro disponível! Iniciando aluguel...",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                Toast.makeText(
                    requireContext(),
                    "Livro indisponível no momento.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // =========================================================
        // BOTÃO PROCURAR
        // =========================================================

        btnProcurar.setOnClickListener {
            requireActivity().finish()
        }

        // =========================================================
        // BOTÃO PDF
        // =========================================================

        btnAbrirPdf.setOnClickListener {

            if (!livro.pdfUrl.isNullOrEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Abrindo Google Books...",
                    Toast.LENGTH_SHORT
                ).show()

                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(livro.pdfUrl))

                startActivity(intent)

            } else {

                Toast.makeText(
                    requireContext(),
                    "Este livro não possui PDF disponível.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // =========================================================
        // BOTÃO AUDIOBOOK
        // =========================================================

        btnAbrirAudio.setOnClickListener {

            Toast.makeText(
                requireContext(),
                "Audiobook indisponível no momento.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================================================
        // BOTÃO RESERVAR
        // =========================================================

        btnReservar.setOnClickListener {

            if (!livro.disponivel) {

                Toast.makeText(
                    requireContext(),
                    "Esse livro já está disponível. Você pode alugá-lo diretamente.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            abrirDialogReserva(livro)
        }
    }

    // =========================================================
    // DIALOG DE RESERVA
    // =========================================================

    private fun abrirDialogReserva(livro: Livro) {

        val dialogView =
            layoutInflater.inflate(R.layout.popup_reserva, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val datePicker =
            dialogView.findViewById<DatePicker>(R.id.datePickerReserva)

        val btnConfirmar =
            dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarReserva)

        val hoje = Calendar.getInstance()

        datePicker.minDate = hoje.timeInMillis

        btnConfirmar.setOnClickListener {

            val dia = datePicker.dayOfMonth
            val mes = datePicker.month + 1
            val ano = datePicker.year

            val dataSelecionada = Calendar.getInstance()

            dataSelecionada.set(
                ano,
                mes - 1,
                dia,
                0,
                0,
                0
            )

            val diferencaMillis =
                dataSelecionada.timeInMillis - hoje.timeInMillis

            val diferencaDias =
                (diferencaMillis / (1000 * 60 * 60 * 24)).toInt()

            if (diferencaDias > 7) {

                Toast.makeText(
                    requireContext(),
                    "A reserva pode ser feita em até 7 dias.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            if (diferencaDias < 0) {

                Toast.makeText(
                    requireContext(),
                    "Escolha uma data válida.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val dataRetirada =
                "%02d/%02d/%04d".format(dia, mes, ano)

            salvarReserva(livro, dataRetirada)

            dialog.dismiss()
        }

        dialog.show()
    }

    // =========================================================
    // SALVAR RESERVA
    // =========================================================

    private fun salvarReserva(
        livro: Livro,
        dataRetirada: String
    ) {

        val sharedPref =
            requireActivity().getSharedPreferences(
                "user_session",
                Context.MODE_PRIVATE
            )

        val emailUsuario =
            sharedPref.getString("USER_EMAIL", "")
                ?.trim()
                ?.lowercase()
                ?: ""

        val reserva = Aluguel(
            email_usuario = emailUsuario,
            titulo_livro = livro.titulo,
            autor_livro = livro.autor,
            capa_url = livro.capaUrl,
            data_vencimento = dataRetirada,
            dias_restantes = 7,
            devolvido = false,
            oculto_historico = false,
            tipo = "RESERVA",
            data_retirada = dataRetirada
        )

        val dataHoraAtual =
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                Locale.getDefault()
            ).format(Date())

        val notificacao = Notificacao(
            email_usuario = emailUsuario,
            titulo = "Reserva Realizada",
            mensagem = "Sua reserva do livro '${livro.titulo}' foi agendada para $dataRetirada.",
            visualizada = false,
            created_at = dataHoraAtual
        )

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                withContext(Dispatchers.IO) {

                    // SALVA RESERVA
                    SupabaseConfig.client
                        .postgrest["alugueis"]
                        .insert(reserva)

                    // SALVA NOTIFICAÇÃO
                    SupabaseConfig.client
                        .postgrest["notificacoes"]
                        .insert(notificacao)
                }

                Toast.makeText(
                    requireContext(),
                    "Reserva realizada com sucesso!",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(
                    requireContext(),
                    "Erro ao realizar reserva.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}