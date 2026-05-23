package com.example.bibliounifornew.adm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdmMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adm_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        // Inicia com o Dashboard caso não seja uma recriação de tela
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, TelaRF28DashboardADM())
                .commit()

            bottomNavigationView.selectedItemId = R.id.nav_dashboard_adm
        }

        // Escuta os cliques da BottomNavigation para trocar as telas (Fragments)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_financeiro -> {
                    trocarFragment(TelaRF34FinanceiroADM())
                    true
                }
                R.id.nav_solicitacoes -> {
                    trocarFragment(Telarf31SolicitacoesADM())
                    true
                }
                R.id.nav_dashboard_adm -> {
                    trocarFragment(TelaRF28DashboardADM())
                    true
                }
                R.id.nav_gerenciamento_usuarios -> {
                    trocarFragment(Telarf29GerenciamentoUsuariosADM())
                    true
                }
                R.id.nav_livros -> {
                    trocarFragment(Telarf32LivrosCrudADM())
                    true
                }
                else -> false
            }
        }
    }

    private fun trocarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}