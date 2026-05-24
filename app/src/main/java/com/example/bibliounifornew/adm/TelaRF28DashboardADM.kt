package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF28DashboardADM : Fragment(R.layout.telarf28_dashboard_adm) {

    // Declarando os componentes baseados no seu novo XML
    private lateinit var iconConfigAdm: ImageView
    private lateinit var buttonCrudAdm: MaterialButton
    private lateinit var buttonVerAlugueis: MaterialButton
    private lateinit var buttonVerAtrasos: MaterialButton
    private lateinit var buttonVerCadastros: MaterialButton
    private lateinit var buttonVerSolicitacoes: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MAPEAMENTO DAS INFORMAÇÕES DO ADM (Sessão)
        val textBemVindoAdm = view.findViewById<TextView>(R.id.textBemVindoAdm)
        val sharedPref = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val nomeAdm = sharedPref.getString("USER_NOME", "Administrador")
        val emailAdm = sharedPref.getString("USER_EMAIL", "")

        textBemVindoAdm.text = "Bem-vindo, $nomeAdm"

        // Verificando existência de cada componente antes de atribuir
        val compIds = mapOf(
            "iconConfigAdm" to R.id.iconConfigAdm,
            "buttonCrudAdm" to R.id.buttonCrudAdm,
            "buttonVerAlugueis" to R.id.buttonVerAlugueis,
            "buttonVerAtrasos" to R.id.buttonVerAtrasos,
            "buttonVerCadastros" to R.id.buttonVerCadastros,
            "buttonVerSolicitacoes" to R.id.buttonVerSolicitacoes
        )

        for ((name, id) in compIds) {
            val v = view.findViewById<View>(id)
            if (v == null) {
                android.util.Log.e("DASHBOARD_FATAL", "COMPONENTE NÃO ENCONTRADO: $name (ID: $id)")
            }
        }

        try {
            iconConfigAdm = view.findViewById(R.id.iconConfigAdm)!!
            buttonCrudAdm = view.findViewById(R.id.buttonCrudAdm)!!
            buttonVerAlugueis = view.findViewById(R.id.buttonVerAlugueis)!!
            buttonVerAtrasos = view.findViewById(R.id.buttonVerAtrasos)!!
            buttonVerCadastros = view.findViewById(R.id.buttonVerCadastros)!!
            buttonVerSolicitacoes = view.findViewById(R.id.buttonVerSolicitacoes)!!

            setupListeners(emailAdm)
        } catch (e: Exception) {
            android.util.Log.e("DASHBOARD_FATAL", "Erro ao inicializar Dashboard: ${e.message}")
        }
    }

    private fun setupListeners(emailAdm: String?) {
        // RF28.2 - Configurações (Botão RF38)
        iconConfigAdm.setOnClickListener {
            val fragment = TelaRF38ConfigADM().apply {
                arguments = Bundle().apply {
                    putString("USER_EMAIL", emailAdm)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }

        // RF28.3 - Tela Inicial, CRUD
        buttonCrudAdm.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf27CrudADM())
                .addToBackStack(null)
                .commit()
        }

        // RF28.5 - Relatório de Aluguéis (Botão RF36)
        buttonVerAlugueis.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf36AlugueisADM())
                .addToBackStack(null)
                .commit()
        }

        // RF28.6 - Livros Atrasados / Financeiro (Botão RF34)
        buttonVerAtrasos.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, TelaRF34FinanceiroADM())
                .addToBackStack(null)
                .commit()
        }

        // RF28.4 - Confirmação de Cadastro (Botão RF35 - Se houver, caso contrário mandamos para usuários)
        buttonVerCadastros.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf29GerenciamentoUsuariosADM())
                .addToBackStack(null)
                .commit()
        }

        // RF28.7 - Solicitações dos Usuários (Botão RF31)
        buttonVerSolicitacoes.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf31SolicitacoesADM())
                .addToBackStack(null)
                .commit()
        }
    }
}