package com.example.bibliounifornew.usuario

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.adapter.DescobrirAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF03LoginAluno
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.Livro
import com.example.bibliounifornew.model.Notificacao
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF08DashboardUsuario : Fragment(R.layout.telarf08_dashboardusuario) {

    private lateinit var recyclerDescobrir: RecyclerView
    private var processandoClique: Boolean = false
    private var emailUsuario: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerDescobrir = view.findViewById(R.id.recyclerDescobrir)
        recyclerDescobrir.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val btnConfig = view.findViewById<ImageView>(R.id.btnConfig)
        val btnNotificacao = view.findViewById<ImageView>(R.id.btnNotificacao)
        val profileImage = view.findViewById<ImageView>(R.id.imagePerfilUsuario)
        val textNomeUsuario = view.findViewById<TextView>(R.id.textNomeUsuario)

        val sharedPref = requireActivity().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)

        val nomeUsuario = sharedPref.getString("USER_NOME", "Usuário")
        emailUsuario = sharedPref.getString("USER_EMAIL", "")?.lowercase()?.trim() ?: ""
        textNomeUsuario.text = nomeUsuario ?: "Usuário"

        val fotoSalvaUrl = sharedPref.getString("USER_FOTO", null)
        if (!fotoSalvaUrl.isNullOrEmpty()) {
            try {
                profileImage?.setImageURI(Uri.parse(fotoSalvaUrl))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Mapeamento dos botões
        val btnPesquisa = view.findViewById<MaterialButton>(R.id.btnPesquisa)
        val btnHistorico = view.findViewById<MaterialButton>(R.id.btnHistorico)
        val btnSair = view.findViewById<MaterialButton>(R.id.btnSairConta)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        btnConfig.setOnClickListener {
            val fragment = TelaRF09Configuracao().apply {
                arguments = Bundle().apply { putString("USER_EMAIL", emailUsuario) }
            }
            irParaFragment(fragment)
        }

        btnNotificacao?.setOnClickListener {
            irParaFragment(TelaRF14Notificacoes())
        }

        btnPesquisa?.setOnClickListener {
            irParaFragment(TelaRF11TelaDePesquisa())
        }


        btnHistorico?.setOnClickListener {
            irParaFragment(TelaRF15Historico())
        }

        btnSair?.setOnClickListener {
            exibirPopupSair()
        }

        carregarLivrosAleatoriosDescobrir()
    }

    private fun carregarLivrosAleatoriosDescobrir() {
        val termosDeBusca = listOf("tecnologia", "romance", "suspense", "historia", "ficcao", "biografias", "misterio", "poesia")
        val termoSorteado = termosDeBusca.random()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resposta = withContext(Dispatchers.IO) {
                    com.example.bibliounifornew.api.RetrofitClient.api.searchBooks(
                        query = termoSorteado,
                        maxResults = 10
                    )
                }
                
                val listaItens = resposta.items

                if (!isAdded) return@launch

                if (!listaItens.isNullOrEmpty()) {
                    val livrosMapeados = listaItens.map { item ->
                        val info = item.volumeInfo
                        val capaUrlHttp = info.imageLinks?.thumbnail ?: ""
                        val capaUrlHttps = capaUrlHttp.replace("http://", "https://")
                        
                        Livro(
                            id = item.id?.hashCode() ?: 0,
                            titulo = info.title ?: "Título Indisponível",
                            autor = info.authors?.joinToString(", ") ?: "Autor Desconhecido",
                            isbn = info.industryIdentifiers?.firstOrNull()?.identifier ?: "Sem ISBN",
                            capaUrl = capaUrlHttps,
                            sinopse = info.description ?: "Sinopse não disponível.",
                            data_publicacao = info.publishedDate ?: "N/I",
                            categoria = info.categories?.firstOrNull() ?: termoSorteado,
                            formato = "Físico",
                            disponivel = true,
                            pdfUrl = null
                        )
                    }
                    
                    recyclerDescobrir.adapter = DescobrirAdapter(
                        livros = livrosMapeados,
                        onAlugarClick = { livroClicado: Livro -> alugarLivroRapido(livroClicado) },
                        onCardClick = { livroClicado: Livro -> abrirDetalhesDoLivro(livroClicado) }
                    )
                } else {
                    context?.let {
                        Toast.makeText(it, "Nenhum livro retornado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded) {
                    context?.let {
                        Toast.makeText(it, "Erro ao carregar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun alugarLivroRapido(livro: Livro) {
        if (processandoClique) return
        processandoClique = true

        if (emailUsuario.isEmpty()) {
            Toast.makeText(requireContext(), "Faça login para alugar", Toast.LENGTH_SHORT).show()
            processandoClique = false
            return
        }

        val formatoData = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val calendario = java.util.Calendar.getInstance()
        calendario.add(java.util.Calendar.DAY_OF_YEAR, 7)
        val dataVencimento = formatoData.format(calendario.time)

        val novoAluguel = Aluguel(
            id = null,
            email_usuario = emailUsuario,
            titulo_livro = livro.titulo,
            autor_livro = livro.autor,
            capa_url = livro.capaUrl,
            data_vencimento = dataVencimento,
            dias_restantes = 7L,
            devolvido = false,
            tipo = "ALUGUEL"
        )

        val dataHoraAtual = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.getDefault())
            .format(java.util.Date())

        val novaNotificacao = Notificacao(
            email_usuario = emailUsuario,
            titulo = "Aluguel Rápido! ⚡",
            mensagem = "Você alugou '${livro.titulo}' direto do Dashboard.",
            visualizada = false,
            created_at = dataHoraAtual
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["alugueis"].insert(novoAluguel)
                    SupabaseConfig.client.postgrest["notificacoes"].insert(novaNotificacao)
                }
                Toast.makeText(requireContext(), "Alugado com sucesso! 🎉", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao salvar aluguel: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                processandoClique = false
            }
        }
    }

    private fun abrirDetalhesDoLivro(livro: Livro) {
        val intent = Intent(requireContext(), TelaRF12TelaDoLivro::class.java).apply {
            putExtra("livro", livro)
        }
        startActivity(intent)
    }

    private fun irParaFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun exibirPopupSair() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_sair_conta, null)
        val alertDialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true).create()
        val btnConfirmarSair = dialogView.findViewById<MaterialButton>(R.id.btnConfirmarSair)

        btnConfirmarSair?.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            alertDialog.dismiss()

            val intent = Intent(requireContext(), TelaRF03LoginAluno::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        alertDialog.show()
    }
}
