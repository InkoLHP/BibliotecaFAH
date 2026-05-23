package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.ImageView
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

        // Mapeando os IDs do XML
        iconConfigAdm = view.findViewById(R.id.iconConfigAdm)
        buttonCrudAdm = view.findViewById(R.id.buttonCrudAdm)
        buttonVerAlugueis = view.findViewById(R.id.buttonVerAlugueis)
        buttonVerAtrasos = view.findViewById(R.id.buttonVerAtrasos)
        buttonVerCadastros = view.findViewById(R.id.buttonVerCadastros)
        buttonVerSolicitacoes = view.findViewById(R.id.buttonVerSolicitacoes)

        // --------------------------------------------------------
        // AÇÕES DOS BOTÕES (Baseado nos Requisitos Funcionais)
        // --------------------------------------------------------

        // RF28.2 - Configurações (Botão RF38)
        iconConfigAdm.setOnClickListener {
            // TODO: Navegar para TelaRF38ConfigADM
        }

        // RF28.3 - Tela Inicial, CRUD
        buttonCrudAdm.setOnClickListener {
            // TODO: Navegar para as Telas de Gerenciamento (CRUD)
        }

        // RF28.5 - Relatório de Aluguéis (Botão RF36)
        buttonVerAlugueis.setOnClickListener {
            // TODO: Navegar para Tela de Aluguéis
        }

        // RF28.6 - Livros Atrasados / Financeiro (Botão RF38 - Nota: verifique se o número do RF não repete o de config)
        buttonVerAtrasos.setOnClickListener {
            // TODO: Navegar para Tela de Livros Atrasados / Financeiro
        }

        // RF28.4 - Confirmação de Cadastro (Botão RF35)
        buttonVerCadastros.setOnClickListener {
            // TODO: Navegar para Tela de Confirmação de Usuários
        }

        // RF28.7 - Solicitações dos Usuários (Botão RF31)
        buttonVerSolicitacoes.setOnClickListener {
            // TODO: Navegar para Tela de Solicitações
        }
    }
}