package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// esse codigo pega os alugues do usuario no supabase e envia os dados pro ReciclerView
class TelaRF13StatusAluguel : Fragment(R.layout.telarf13_status) {

    private lateinit var recyclerAlugueis: RecyclerView
    private lateinit var textNenhumLivro: TextView

    private var emailUsuario: String = ""

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        recyclerAlugueis =
            view.findViewById(R.id.recyclerAlugueis)

        textNenhumLivro =
            view.findViewById(R.id.textNenhumLivro)

        recyclerAlugueis.layoutManager =
            LinearLayoutManager(requireContext())

        // EMAIL DO USUÁRIO LOGADO
        val sharedPref =
            requireActivity().getSharedPreferences(
                "user_session",
                android.content.Context.MODE_PRIVATE
            )

        emailUsuario =
            sharedPref.getString(
                "USER_EMAIL",
                ""
            ) ?: ""

        carregarAlugueis()
    }

    private fun carregarAlugueis() {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val alugueis =
                    withContext(Dispatchers.IO) {

                        SupabaseConfig.client
                            .postgrest["alugueis"]
                            .select {

                                filter {

                                    eq(
                                        "email_usuario",
                                        emailUsuario
                                    )
                                }
                            }
                            .decodeList<Aluguel>()
                    }

                if (alugueis.isEmpty()) {

                    textNenhumLivro.visibility =
                        View.VISIBLE

                    recyclerAlugueis.visibility =
                        View.GONE

                } else {

                    textNenhumLivro.visibility =
                        View.GONE

                    recyclerAlugueis.visibility =
                        View.VISIBLE

                    recyclerAlugueis.adapter =
                        AluguelAdapter(alugueis)
                }

            } catch (e: Exception) {

                e.printStackTrace()

                textNenhumLivro.visibility =
                    View.VISIBLE

                textNenhumLivro.text =
                    "Erro ao carregar aluguéis"
            }
        }
    }
}