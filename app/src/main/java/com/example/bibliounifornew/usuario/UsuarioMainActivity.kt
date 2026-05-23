package com.example.bibliounifornew.usuario

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bibliounifornew.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class UsuarioMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_usuario_main)

        val bottomNavigationView =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigationView
            )

        if (savedInstanceState == null) {

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.frameLayout,
                    TelaRF08DashboardUsuario()
                )
                .commit()

            bottomNavigationView.selectedItemId =
                R.id.nav_dashboard
        }

        bottomNavigationView.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_status -> {
                    trocarFragment(TelaRF13StatusAluguel())
                    true
                }

                R.id.nav_pesquisa -> {
                    trocarFragment(TelaRF11TelaDePesquisa())
                    true
                }

                R.id.nav_dashboard -> {
                    trocarFragment(TelaRF08DashboardUsuario())
                    true
                }

                R.id.nav_livraria -> {
                    trocarFragment(TelaRF15MinhaLivraria())
                    true
                }

                R.id.nav_amigos -> {
                    trocarFragment(TelaRF17Amigos())
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