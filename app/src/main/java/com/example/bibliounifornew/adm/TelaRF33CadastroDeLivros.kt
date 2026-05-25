package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R

class TelaRF33CadastroDeLivros : Fragment(R.layout.telarf33_cadastro_livro) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vinculando os componentes do Layout
        val editTitulo = view.findViewById<EditText>(R.id.etTitulo)
        val editAutor = view.findViewById<EditText>(R.id.etAutor)
        val editISBN = view.findViewById<EditText>(R.id.etISBN)
        val editData = view.findViewById<EditText>(R.id.etData)
        val editExemplares = view.findViewById<EditText>(R.id.etQuantidade)
        val tvErro = view.findViewById<TextView>(R.id.tvErro) // Mapeando o texto de erro
        val buttonEditarMais = view.findViewById<Button>(R.id.btnEditarMaisInformacoes)

        // Evento de clique no campo de data para abrir o pop-up customizado
        editData.setOnClickListener {
            abrirPopupData(editData)
        }

        // Ação de avançar para a próxima tela
        buttonEditarMais.setOnClickListener {

            // 1. Validação: Verifica se tem algum campo vazio
            if (editTitulo.text.isBlank() || editAutor.text.isBlank() ||
                editISBN.text.isBlank() || editData.text.isBlank() || editExemplares.text.isBlank()) {

                // Mostra a mensagem vermelha de erro definida no XML
                tvErro.visibility = View.VISIBLE
                return@setOnClickListener // Para a execução aqui
            }

            // Esconde o erro caso tudo esteja preenchido
            tvErro.visibility = View.GONE

            // 2. Prepara o próximo Fragment e empacota os dados
            val fragment = TelaRF33CadastroMaisInformacoes().apply {
                arguments = Bundle().apply {
                    putString("TITULO", editTitulo.text.toString().trim())
                    putString("AUTOR", editAutor.text.toString().trim())
                    putString("ISBN", editISBN.text.toString().trim())
                    putString("DATA", editData.text.toString().trim())
                    putString("EXEMPLARES", editExemplares.text.toString().trim())
                }
            }

            // 3. Executa a navegação
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment) // Confirme se o seu container principal se chama "frameLayout"
                .addToBackStack(null) // Permite voltar para essa tela sem perder o que digitou
                .commit()
        }
    }

    private fun abrirPopupData(editDataTarget: EditText) {
        // Infla o layout do pop-up usando requireContext()
        val viewPopup = LayoutInflater.from(requireContext()).inflate(R.layout.popup_data_publicacao, null)

        val datePicker = viewPopup.findViewById<DatePicker>(R.id.datePicker)
        val btnConfirmar = viewPopup.findViewById<Button>(R.id.btnConfirmarData)

        // Cria o AlertDialog usando o contexto do Fragment
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        builder.setView(viewPopup)

        val dialog = builder.create()

        // Configura o clique do botão Confirmar
        btnConfirmar.setOnClickListener {
            val dia = datePicker.dayOfMonth
            val mes = datePicker.month + 1 // O Android conta os meses de 0 a 11
            val ano = datePicker.year

            // Formata a data para DD/MM/AAAA
            val dataFormatada = String.format("%02d/%02d/%04d", dia, mes, ano)

            // Joga a data de volta para o EditText da tela principal
            editDataTarget.setText(dataFormatada)

            // Fecha o pop-up
            dialog.dismiss()
        }

        dialog.show()
    }
}