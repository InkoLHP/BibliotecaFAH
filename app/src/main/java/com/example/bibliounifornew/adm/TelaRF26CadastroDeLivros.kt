package com.example.bibliounifornew.adm

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF26CadastroDeLivros : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf26_cadastro_livro)

        // Vinculando os componentes do Layout (IDs atualizados do padrão premium)
        val editTitulo = findViewById<EditText>(R.id.etTitulo)
        val editAutor = findViewById<EditText>(R.id.etAutor)
        val editISBN = findViewById<EditText>(R.id.etISBN)
        val editData = findViewById<EditText>(R.id.etData)
        val editExemplares = findViewById<EditText>(R.id.etQuantidade)
        val buttonEditarMais = findViewById<Button>(R.id.btnEditarMaisInformacoes)

        // Evento de clique no campo de data para abrir o pop-up customizado
        editData.setOnClickListener {
            abrirPopupData(editData)
        }

        buttonEditarMais.setOnClickListener {
            val intent = Intent(this, TelaRF27CadastroMaisInformacoes::class.java)
            startActivity(intent)

            // acho que precisa ter alguma coisa de banco de dados aqui pra "carregar" as informações pra prox tela
        }
    }

    private fun abrirPopupData(editDataTarget: EditText) {
        // Infla o layout do pop-up premium que criamos
        val viewPopup = LayoutInflater.from(this).inflate(R.layout.popup_data_publicacao, null)

        val datePicker = viewPopup.findViewById<DatePicker>(R.id.datePicker)
        val btnConfirmar = viewPopup.findViewById<Button>(R.id.btnConfirmarData)

        // Cria o AlertDialog com o estilo transparente para herdar o bg_input arredondado
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setView(viewPopup)

        val dialog = builder.create()

        // Configura o clique do botão Confirmar
        btnConfirmar.setOnClickListener {
            val dia = datePicker.dayOfMonth
            val mes = datePicker.month + 1 // O Git/Android conta os meses de 0 a 11, por isso o +1
            val ano = datePicker.year

            // Formata a data para ficar bonitinha com duas casas decimais (DD/MM/AAAA)
            val dataFormatada = String.format("%02d/%02d/%04d", dia, mes, ano)

            // Joga a data de volta para o EditText da tela principal
            editDataTarget.setText(dataFormatada)

            // Fecha o pop-up
            dialog.dismiss()
        }

        dialog.show()
    }
}