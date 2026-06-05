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

        val editTitulo = view.findViewById<EditText>(R.id.etTitulo)
        val editAutor = view.findViewById<EditText>(R.id.etAutor)
        val editISBN = view.findViewById<EditText>(R.id.etISBN)
        val editData = view.findViewById<EditText>(R.id.etData)
        val editExemplares = view.findViewById<EditText>(R.id.etQuantidade)
        val tvErro = view.findViewById<TextView>(R.id.tvErro)
        val buttonEditarMais = view.findViewById<Button>(R.id.btnEditarMaisInformacoes)

        editData.setOnClickListener { abrirPopupData(editData) }

        buttonEditarMais.setOnClickListener {
            if (editTitulo.text.isBlank() || editAutor.text.isBlank() ||
                editISBN.text.isBlank() || editData.text.isBlank() || editExemplares.text.isBlank()) {
                tvErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvErro.visibility = View.GONE

            // Envia para a Tela 2 usando chaves padronizadas em minúsculas
            val fragment = TelaRF33CadastroMaisInformacoes().apply {
                arguments = Bundle().apply {
                    putString("titulo", editTitulo.text.toString().trim())
                    putString("autor", editAutor.text.toString().trim())
                    putString("isbn", editISBN.text.toString().trim())
                    putString("data", editData.text.toString().trim())
                    putString("exemplares", editExemplares.text.toString().trim())
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun abrirPopupData(editDataTarget: EditText) {
        val viewPopup = LayoutInflater.from(requireContext()).inflate(R.layout.popup_data_publicacao, null)
        val datePicker = viewPopup.findViewById<DatePicker>(R.id.datePicker)
        val btnConfirmar = viewPopup.findViewById<Button>(R.id.btnConfirmarData)

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).setView(viewPopup)
        val dialog = builder.create()

        btnConfirmar.setOnClickListener {
            val dataFormatada = String.format("%02d/%02d/%04d", datePicker.dayOfMonth, datePicker.month + 1, datePicker.year)
            editDataTarget.setText(dataFormatada)
            dialog.dismiss()
        }
        dialog.show()
    }
}