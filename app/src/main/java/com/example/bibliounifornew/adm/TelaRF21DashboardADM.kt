package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF02Intermediaria
import com.example.bibliounifornew.usuario.TelaRF14Notificacoes

class TelaRF21DashboardADM : AppCompatActivity() {

    private lateinit var recyclerMidias: RecyclerView
    private lateinit var etProcurarMidia: EditText
    private lateinit var buttonPesquisar: Button
    private lateinit var bntConfiguracaoADM: ImageView
    private lateinit var bntNotificacoesADM: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf21_dashboard_adm)

        recyclerMidias = findViewById(R.id.recyclerMidias)
        etProcurarMidia = findViewById(R.id.etProcurarMidia)
        bntConfiguracaoADM = findViewById(R.id.iconConfiguracao)
        bntNotificacoesADM = findViewById(R.id.iconNotificacao)

        recyclerMidias.layoutManager = LinearLayoutManager(this)

        // LISTA COMEÇA VAZIA

        recyclerMidias.adapter = MidiaAdminAdapter(emptyList())

        // PESQUISA TEMPORÁRIA

        etProcurarMidia.setOnEditorActionListener { _, _, _ ->

            pesquisarMidias()

            true
        }

       /* bntNotificacoesADM.setOnClickListener {
            val intent = Intent(this, TelaRF02Intermediaria::class.java)
            startActivity(intent)

        }*/

        bntConfiguracaoADM.setOnClickListener {
            val intent = Intent(this, TelaRF22ConfigADM::class.java)
            startActivity(intent)

        }
    }

    private fun pesquisarMidias() {

        val textoPesquisa = etProcurarMidia.text.toString()

        // TEMPORÁRIO
        // depois tu troca pelo banco

        val listaFake = listOf(
            Midia(
                "O Senhor dos Anéis",
                "J. R. R. Tolkien",
                "8595084750"
            ),
            Midia(
                "Dom Casmurro",
                "Machado de Assis",
                "9786559212466"
            ),
            Midia(
                "Percy Jackson",
                "Rick Riordan",
                "9788598078397"
            )
        )

        // FILTRO SIMPLES

        val resultado = listaFake.filter {

            it.titulo.contains(textoPesquisa, true) ||
                    it.autor.contains(textoPesquisa, true) ||
                    it.isbn.contains(textoPesquisa, true)
        }

        recyclerMidias.adapter = MidiaAdminAdapter(resultado)
    }
}