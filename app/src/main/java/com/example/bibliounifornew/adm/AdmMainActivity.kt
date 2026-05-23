package com.example.bibliounifornew.adm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdmMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lembre-se de criar esse layout (activity_adm_main.xml)
        // contendo o FrameLayout e o BottomNavigationView do ADM
        setContentView(R.layout.activity_adm_main)

        // Cuidado para usar o ID correto da barra do ADM aqui
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavAdm)

        // Carrega o Dashboard ADM como tela inicial assim que o app abre
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayoutAdm, TelaRF28DashboardADM())
                .commit()

            // Marca o ícone do Dashboard como selecionado
            bottomNavigationView.selectedItemId = R.id.nav_dashboard_adm
        }

        bottomNavigationView.setOnItemSelectedListener { item ->

            when (item.itemId) {

                //Trocar eventualmente
                // RF28 - Dashboard
                R.id.nav_dashboard_adm -> {
                    trocarFragment(TelaRF28DashboardADM())
                    true
                }

                // RF34 - Financeiro
                R.id.nav_financeiro_adm -> {
                    trocarFragment(TelaRF34FinanceiroADM())
                    true
                }

                // RF31 - Solicitações
                R.id.nav_solicitacoes_adm -> {
                    // TODO: Trocar pelo nome exato do seu Fragment de Solicitações
                    trocarFragment(TelaRF31SolicitacoesADM())
                    true
                }

                // RF29 - Gerenciamento de Usuários
                R.id.nav_usuarios_adm -> {
                    // TODO: Trocar pelo nome exato do seu Fragment de Gerenciamento de Usuários
                    trocarFragment(TelaRF29GerenciarUsuariosADM())
                    true
                }

                // RF32 - Livros / CRUD (Coloquei a de cadastro como exemplo)
                R.id.nav_livros_adm -> {
                    trocarFragment(TelaRF33CadastroDeLivros())
                    true
                }

                else -> false
            }
        }
    }

    private fun trocarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayoutAdm, fragment)
            .commit()
    }
}