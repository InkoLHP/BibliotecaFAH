package com.example.bibliounifornew.usuario

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Aluguel
import com.example.bibliounifornew.model.Livro
import com.example.bibliounifornew.model.Notificacao
import com.example.bibliounifornew.model.Solicitacao
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import com.example.bibliounifornew.data.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private var isDisponivel: Boolean = false
    private var quantidadeEstoque: Int = 0
    private var processandoClique: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf12_tela_livro)

        progressBar = findViewById(R.id.progressBarDetalhes)

        val livro = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("livro", Livro::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("livro") as? Livro
        }

        if (livro != null) {
            mostrarLivro(livro)
            configurarEstoqueEAluguel(livro)
            configurarBotaoSolicitar(livro)
            configurarMarcadoresDeLeitura(livro)
        } else {
            Toast.makeText(this, "Livro não encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.buttonLer).setOnClickListener {
            val fragmentLeitura = TelaRF14Leitura()
            val bundle = Bundle()
            bundle.putSerializable("livro", livro)
            fragmentLeitura.arguments = bundle

            supportFragmentManager.beginTransaction()
                .replace(R.id.main_detalhes_container, fragmentLeitura)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun mostrarLivro(livro: Livro) {
        findViewById<TextView>(R.id.textTituloLivro).text = livro.titulo
        findViewById<TextView>(R.id.textAutorLivro).text = livro.autor
        findViewById<TextView>(R.id.textSobreLivro).text = livro.sinopse ?: "Sinopse não disponível."

        findViewById<ImageView>(R.id.imageLivroDetalhes).load(livro.capaUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }

        findViewById<TextView>(R.id.textGeneroLivro).text = livro.categoria ?: "N/I"
        findViewById<TextView>(R.id.textDataLivro).text = livro.data_publicacao ?: "N/I"
        findViewById<TextView>(R.id.textIsbnLivro).text = livro.isbn

        findViewById<TextView>(R.id.textIdiomaLivro).text = "N/I"
        findViewById<TextView>(R.id.textEditoraLivro).text = "N/I"
        findViewById<TextView>(R.id.textDimensaoLivro).text = "N/I"
        findViewById<TextView>(R.id.textPaginasLivro).text = "N/I"

        val eDigital = livro.formato.contains("pdf", ignoreCase = true) ||
                livro.formato.contains("epub", ignoreCase = true)
        findViewById<TextView>(R.id.textPdfDisponivel).text = if (eDigital) "Sim" else "Não"
    }

    private fun configurarEstoqueEAluguel(livro: Livro) {
        val textDisponibilidade = findViewById<TextView>(R.id.textDisponibilidade)
        val textQuantidadeEstoque = findViewById<TextView>(R.id.textQuantidadeEstoque)
        val buttonAlugar = findViewById<Button>(R.id.buttonAlugar)

        isDisponivel = Random.nextDouble() < 0.75
        quantidadeEstoque = if (isDisponivel) Random.nextInt(1, 6) else 0

        if (isDisponivel && quantidadeEstoque > 0) {
            textDisponibilidade.text = "Sim"
            textDisponibilidade.setTextColor(ContextCompat.getColor(this, R.color.biblio_blue))
            textQuantidadeEstoque.text = quantidadeEstoque.toString()
        } else {
            textDisponibilidade.text = "Não Disponível"
            textDisponibilidade.setTextColor(Color.RED)
            textQuantidadeEstoque.text = "0"
        }

        buttonAlugar.setOnClickListener {
            if (processandoClique) return@setOnClickListener

            if (isDisponivel && (quantidadeEstoque > 0)) {
                processandoClique = true

                quantidadeEstoque--
                textQuantidadeEstoque.text = quantidadeEstoque.toString()

                if (quantidadeEstoque == 0) {
                    isDisponivel = false
                    textDisponibilidade.text = "Não Disponível"
                    textDisponibilidade.setTextColor(Color.RED)
                }

                val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
                val emailReal = sharedPref.getString("USER_EMAIL", "")?.lowercase()?.trim() ?: ""

                if (emailReal.isEmpty()) {
                    Toast.makeText(this, "Faça login para alugar", Toast.LENGTH_SHORT).show()
                    processandoClique = false
                    return@setOnClickListener
                }

                val formatoData = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val calendario = java.util.Calendar.getInstance()
                calendario.add(java.util.Calendar.DAY_OF_YEAR, 7)
                val dataVencimento = formatoData.format(calendario.time)

                val novoAluguel = Aluguel(
                    id = null,
                    email_usuario = emailReal,
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
                    email_usuario = emailReal,
                    titulo = "Aluguel Confirmado! 📚",
                    mensagem = "Você alugou '${livro.titulo}'. Vencimento: $dataVencimento.",
                    visualizada = false,
                    created_at = dataHoraAtual
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        SupabaseConfig.client.postgrest["alugueis"].insert(novoAluguel)
                        SupabaseConfig.client.postgrest["notificacoes"].insert(novaNotificacao)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TelaRF12TelaDoLivro, "Aluguel salvo no banco! 🎉", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TelaRF12TelaDoLivro, "Erro ao salvar aluguel: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } finally {
                        processandoClique = false
                    }
                }

                dispararNotificacaoLocal("Aluguel Confirmado! 📚", "O livro '${livro.titulo}' foi reservado. Vencimento: $dataVencimento.")

            } else {
                Toast.makeText(this, "Desculpe, este livro não está disponível no estoque no momento.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configurarBotaoSolicitar(livro: Livro) {
        val buttonSolicitar = findViewById<Button>(R.id.buttonSolicitarLivro)

        buttonSolicitar.setOnClickListener {
            val opcoes = arrayOf(
                "Solicitar Livro Físico",
                "Solicitar PDF / Versão Digital",
                "Solicitar Audiobook"
            )

            AlertDialog.Builder(this)
                .setTitle("Escolha o tipo de solicitação")
                .setItems(opcoes) { _, itemSelecionado ->
                    when (itemSelecionado) {
                        0 -> enviarSolicitacaoReal(livro, "LIVRO_FISICO", "do livro físico")
                        1 -> enviarSolicitacaoReal(livro, "PDF_DIGITAL", "do PDF / Versão Digital")
                        2 -> enviarSolicitacaoReal(livro, "AUDIO_BOOK", "do Audiobook")
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun enviarSolicitacaoReal(livro: Livro, tipoSolicitacao: String, textoMensagem: String) {
        if (processandoClique) return
        processandoClique = true

        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val emailReal = sharedPref.getString("USER_EMAIL", "")?.lowercase()?.trim() ?: ""

        if (emailReal.isEmpty()) {
            Toast.makeText(this, "Faça login para solicitar", Toast.LENGTH_SHORT).show()
            processandoClique = false
            return
        }

        val dataHoraAtual = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.getDefault())
            .format(java.util.Date())

        val novaSolicitacao = Solicitacao(
            id = null,
            titulo = livro.titulo,
            autor = livro.autor,
            email_usuario = emailReal,
            tipo_solicitacao = tipoSolicitacao,
            capa_url = livro.capaUrl,
            status = "PENDENTE"
        )

        val novaNotificacao = Notificacao(
            email_usuario = emailReal,
            titulo = "Solicitação Enviada ⏳",
            mensagem = "Sua solicitação $textoMensagem para o livro '${livro.titulo}' foi enviada ao administrador.",
            visualizada = false,
            created_at = dataHoraAtual
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                SupabaseConfig.client.postgrest["solicitacoes"].insert(novaSolicitacao)
                SupabaseConfig.client.postgrest["notificacoes"].insert(novaNotificacao)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TelaRF12TelaDoLivro, "Solicitação enviada com sucesso! ⏳", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TelaRF12TelaDoLivro, "Erro ao salvar Solicitação: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                processandoClique = false
            }
        }

        dispararNotificacaoLocal("Solicitação Enviada", "Sua mensagem sobre '${livro.titulo}' está aguardando aprovação.")
    }

    private fun configurarMarcadoresDeLeitura(livro: Livro) {
        val buttonNaoLido = findViewById<MaterialButton>(R.id.buttonNaoLido)
        val buttonLendo = findViewById<MaterialButton>(R.id.buttonLendo)
        val buttonLido = findViewById<MaterialButton>(R.id.buttonLido)

        val sharedPrefs = getSharedPreferences("user_session", MODE_PRIVATE)

        val livroIdentificador = livro.id ?: "0"
        val statusSalvo = sharedPrefs.getString("status_$livroIdentificador", "NAO_LIDO")

        atualizarVisualBotoesLeitura(statusSalvo, buttonNaoLido, buttonLendo, buttonLido)

        buttonNaoLido.setOnClickListener {
            sharedPrefs.edit().putString("status_$livroIdentificador", "NAO_LIDO").apply()
            atualizarVisualBotoesLeitura("NAO_LIDO", buttonNaoLido, buttonLendo, buttonLido)
            Toast.makeText(this, "Marcado como: Não Lido", Toast.LENGTH_SHORT).show()
        }

        buttonLendo.setOnClickListener {
            sharedPrefs.edit().putString("status_$livroIdentificador", "LENDO").apply()
            atualizarVisualBotoesLeitura("LENDO", buttonNaoLido, buttonLendo, buttonLido)
            Toast.makeText(this, "Marcado como: Lendo", Toast.LENGTH_SHORT).show()
        }

        buttonLido.setOnClickListener {
            sharedPrefs.edit().putString("status_$livroIdentificador", "LIDO").apply()
            atualizarVisualBotoesLeitura("LIDO", buttonNaoLido, buttonLendo, buttonLido)
            Toast.makeText(this, "Marcado como: Lido! 📚", Toast.LENGTH_SHORT).show()
        }
    }

    private fun atualizarVisualBotoesLeitura(
        status: String?,
        btnNaoLido: MaterialButton,
        btnLendo: MaterialButton,
        btnLido: MaterialButton
    ) {
        val corSelecionado = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.biblio_blue))
        val corPadrao = ColorStateList.valueOf(Color.parseColor("#80415E5E"))

        btnNaoLido.backgroundTintList = corPadrao
        btnLendo.backgroundTintList = corPadrao
        btnLido.backgroundTintList = corPadrao

        when (status) {
            "NAO_LIDO" -> btnNaoLido.backgroundTintList = corSelecionado
            "LENDO" -> btnLendo.backgroundTintList = corSelecionado
            "LIDO" -> btnLido.backgroundTintList = corSelecionado
        }
    }

    private fun dispararNotificacaoLocal(titulo: String, message: String) {
        val channelId = "canal_biblioteca"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal =
                NotificationChannel(channelId, "Notificações BiblioUnifor", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }

        val construtor = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.placeholder)
            .setContentTitle(titulo)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random.nextInt(), construtor.build())
    }
}
