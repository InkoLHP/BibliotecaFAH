package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R

class TelaRF33CadastroDeLivros : Fragment(R.layout.telarf33_cadastro_livro) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vinculando os componentes do Layout (adicionando 'view.' antes)
        val editTitulo = view.findViewById<EditText>(R.id.etTitulo)
        val editAutor = view.findViewById<EditText>(R.id.etAutor)
        val editISBN = view.findViewById<EditText>(R.id.etISBN)
        val editData = view.findViewById<EditText>(R.id.etData)
        val editExemplares = view.findViewById<EditText>(R.id.etQuantidade)
        val buttonEditarMais = view.findViewById<Button>(R.id.btnEditarMaisInformacoes)

        // Evento de clique no campo de data para abrir o pop-up customizado
        editData.setOnClickListener {
            abrirPopupData(editData)
        }

        buttonEditarMais.setOnClickListener {
            // TODO: Inserir a navegação do Fragment para a TelaRF33CadastroMaisInformacoes
            // Aqui você poderá passar os dados preenchidos usando Bundle ou SafeArgs
        }
    }

    private fun abrirPopupData(editDataTarget: EditText) {
        // Infla o layout do pop-up usando requireContext()
        val viewPopup = LayoutInflater.from(requireContext()).inflate(R.layout.popup_data_publicacao, null)

        val datePicker = viewPopup.findViewById<DatePicker>(R.id.datePicker)
        val btnConfirmar = viewPopup.findViewById<Button>(R.id.btnConfirmarData)

        // Cria o AlertDialog usando o contexto do Fragment (requireContext())
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        builder.setView(viewPopup)

        val dialog = builder.create()

        // Configura o clique do botão Confirmar
        btnConfirmar.setOnClickListener {
            val dia = datePicker.dayOfMonth
            val mes = datePicker.month + 1 // O Android conta os meses de 0 a 11, por isso o +1
            val ano = datePicker.year

            // Formata a data para ficar bonitinha (DD/MM/AAAA)
            val dataFormatada = String.format("%02d/%02d/%04d", dia, mes, ano)

            // Joga a data de volta para o EditText da tela principal
            editDataTarget.setText(dataFormatada)

            // Fecha o pop-up
            dialog.dismiss()
        }

        dialog.show()
    }
}