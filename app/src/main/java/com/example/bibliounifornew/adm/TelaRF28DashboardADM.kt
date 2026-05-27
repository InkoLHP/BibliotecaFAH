package com.example.bibliounifornew.adm

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs

class TelaRF28DashboardADM : Fragment(R.layout.telarf28_dashboard_adm) {

    private lateinit var iconConfigAdm: ImageView
    private lateinit var buttonCrudAdm: MaterialButton
    private lateinit var buttonVerAlugueis: MaterialButton
    private lateinit var buttonVerAtrasos: MaterialButton
    private lateinit var buttonVerSolicitacoes: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SESSÃO DO USUÁRIO ADM
        val textBemVindoAdm = view.findViewById<TextView>(R.id.textBemVindoAdm)
        val sharedPref = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val nomeAdm = sharedPref.getString("USER_NOME", "Administrador")
        val emailAdm = sharedPref.getString("USER_EMAIL", "")

        textBemVindoAdm.text = "Bem-vindo, $nomeAdm"

        // INICIALIZAÇÃO DE COMPONENTES DE NAVEGAÇÃO
        try {
            iconConfigAdm = view.findViewById(R.id.iconConfigAdm)!!
            buttonCrudAdm = view.findViewById(R.id.buttonCrudAdm)!!
            buttonVerAlugueis = view.findViewById(R.id.buttonVerAlugueis)!!
            buttonVerAtrasos = view.findViewById(R.id.buttonVerAtrasos)!!
            buttonVerSolicitacoes = view.findViewById(R.id.buttonVerSolicitacoes)!!

            setupListeners(emailAdm)
            carregarDadosDoBanco(view)
        } catch (e: Exception) {
            android.util.Log.e("DASHBOARD_FATAL", "Erro ao inicializar Dashboard: ${e.message}")
        }
    }

    private fun carregarDadosDoBanco(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Busca dados de Aluguéis e Usuários de forma assíncrona
                val todosAlugueis = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("alugueis").select().decodeList<Aluguel>()
                }
                val todosUsuarios = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("users").select().decodeList<User>()
                }

                // --- CARD 1 & CARD 2: CÁLCULOS DE METRICAS ---
                val totalUsers = todosUsuarios.size
                val totalRentals = todosAlugueis.size
                val ativos = todosAlugueis.count { !it.devolvido }
                val devolvidos = todosAlugueis.count { it.devolvido }

                // Atualizando os textos na UI
                view.findViewById<TextView>(R.id.txtTotalUsuarios).text = totalUsers.toString()
                view.findViewById<TextView>(R.id.txtTotalAlugueis).text = totalRentals.toString()
                view.findViewById<TextView>(R.id.txtAlugueisAtivos).text = ativos.toString()
                view.findViewById<TextView>(R.id.txtAlugueisDevolvidos).text = devolvidos.toString()
                view.findViewById<TextView>(R.id.txtTotalHistorico).text = totalRentals.toString()

                // --- CARD 3: ATRASOS CRÍTICOS (Filtra quem tem dias_restantes negativo) ---
                val alugueisAtrasados = todosAlugueis.filter {
                    !it.devolvido && it.dias_restantes != null && it.dias_restantes < 0
                }.sortedBy { it.dias_restantes } // Traz o maior atraso primeiro

                if (alugueisAtrasados.isNotEmpty()) {
                    val a1 = alugueisAtrasados[0]
                    view.findViewById<TextView>(R.id.txtAtrasoNome1).text = a1.titulo_livro
                    view.findViewById<TextView>(R.id.txtAtrasoDias1).text = "${abs(a1.dias_restantes!!)} dias"

                    if (alugueisAtrasados.size > 1) {
                        val a2 = alugueisAtrasados[1]
                        view.findViewById<TextView>(R.id.txtAtrasoNome2).text = a2.titulo_livro
                        view.findViewById<TextView>(R.id.txtAtrasoDias2).text = "${abs(a2.dias_restantes!!)} dias"
                    }
                }

                // --- CARD 5: SOLICITAÇÕES PENDENTES (Busca Genérica e Segura) ---
                try {
                    val listaSolicitacoes = withContext(Dispatchers.IO) {
                        SupabaseConfig.client.from("solicitacoes").select().decodeList<JsonObject>()
                    }
                    if (listaSolicitacoes.isNotEmpty()) {
                        val s1 = listaSolicitacoes[0]
                        view.findViewById<TextView>(R.id.txtSolicitacaoNome1).text = s1["titulo"]?.jsonPrimitive?.content ?: "Solicitação S/N"
                        view.findViewById<TextView>(R.id.txtSolicitacaoTipo1).text = s1["tipo"]?.jsonPrimitive?.content ?: "Padrão"

                        if (listaSolicitacoes.size > 1) {
                            val s2 = listaSolicitacoes[1]
                            view.findViewById<TextView>(R.id.txtSolicitacaoNome2).text = s2["titulo"]?.jsonPrimitive?.content ?: "Solicitação S/N"
                            view.findViewById<TextView>(R.id.txtSolicitacaoTipo2).text = s2["tipo"]?.jsonPrimitive?.content ?: "Padrão"
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("DASHBOARD", "Tabela 'solicitacoes' não encontrada ou vazia.")
                }

            } catch (e: Exception) {
                android.util.Log.e("DASHBOARD_ERR", "Erro ao carregar dados do Supabase: ${e.message}")
            }
        }
    }

    private fun setupListeners(emailAdm: String?) {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        iconConfigAdm.setOnClickListener {
            val fragment = TelaRF38ConfigADM().apply {
                arguments = Bundle().apply { putString("USER_EMAIL", emailAdm) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }

        buttonCrudAdm.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf27CrudADM())
                .addToBackStack(null)
                .commit()
        }

        buttonVerAlugueis.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf36AlugueisADM())
                .addToBackStack(null)
                .commit()
        }

        buttonVerAtrasos.setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_financeiro
        }

        buttonVerSolicitacoes.setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_solicitacoes
        }
    }
}