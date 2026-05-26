package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.model.Livro
import com.google.android.material.button.MaterialButton
import kotlin.random.Random

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    // Variáveis para controlar a regra do estoque
    private var isDisponivel: Boolean = false
    private var quantidadeEstoque: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf12_tela_livro)

        progressBar = findViewById(R.id.progressBarDetalhes)

        // Usando o seu modelo de dados Livro vindo da Intent
        val livro = intent.getSerializableExtra("livro") as? Livro

        if (livro != null) {
            mostrarLivro(livro)
            configurarEstoqueEAluguel()
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
        // 1. Dados principais (Mapeamento Direto do seu Modelo)
        findViewById<TextView>(R.id.textTituloLivro).text = livro.titulo
        findViewById<TextView>(R.id.textAutorLivro).text = livro.autor
        findViewById<TextView>(R.id.textSobreLivro).text = livro.sinopse ?: "Sinopse não disponível."

        // 2. Carregamento da Capa usando Coil (mantendo seu padrão)
        findViewById<ImageView>(R.id.imageLivroDetalhes).load(livro.capaUrl) {
            crossfade(true)
            placeholder(R.drawable.osda)
            error(R.drawable.osda)
        }

        // 3. Mapeando atributos reais presentes no seu Model 'Livro'
        findViewById<TextView>(R.id.textGeneroLivro).text = livro.categoria ?: "N/I"
        findViewById<TextView>(R.id.textDataLivro).text = livro.data_publicacao ?: "N/I"
        findViewById<TextView>(R.id.textIsbnLivro).text = livro.isbn

        // 4. Tratamento dos campos que o layout XML exige, mas não estão no Model.
        // Definimos padrões inteligentes para preencher a UI e evitar erros de compilação.
        findViewById<TextView>(R.id.textIdiomaLivro).text = "N/I"
        findViewById<TextView>(R.id.textEditoraLivro).text = "N/I"
        findViewById<TextView>(R.id.textDimensaoLivro).text = "N/I"
        findViewById<TextView>(R.id.textPaginasLivro).text = "N/I"

        // Verificação dinâmica baseada no campo 'formato' que você já possui no modelo
        val eDigital = livro.formato?.contains("pdf", ignoreCase = true) == true ||
                livro.formato?.contains("epub", ignoreCase = true) == true
        findViewById<TextView>(R.id.textPdfDisponivel).text = if (eDigital) "Sim" else "Não"
    }

    private fun configurarEstoqueEAluguel() {
        val textDisponibilidade = findViewById<TextView>(R.id.textDisponibilidade)
        val textQuantidadeEstoque = findViewById<TextView>(R.id.textQuantidadeEstoque)
        val buttonAlugar = findViewById<Button>(R.id.buttonAlugar)

        // Regra de Negócio: 75% de chance de estar disponível
        isDisponivel = Random.nextDouble() < 0.75
        quantidadeEstoque = if (isDisponivel) Random.nextInt(1, 6) else 0

        // Atualiza a UI com os dados gerados
        if (isDisponivel && quantidadeEstoque > 0) {
            textDisponibilidade.text = "Sim"
            textDisponibilidade.setTextColor(Color.parseColor("#415E5E"))
            textQuantidadeEstoque.text = quantidadeEstoque.toString()
        } else {
            textDisponibilidade.text = "Não Disponível"
            textDisponibilidade.setTextColor(Color.RED)
            textQuantidadeEstoque.text = "0"
        }

        // Evento de clique do botão Alugar
        buttonAlugar.setOnClickListener {
            if (isDisponivel && quantidadeEstoque > 0) {
                quantidadeEstoque--
                textQuantidadeEstoque.text = quantidadeEstoque.toString()

                if (quantidadeEstoque == 0) {
                    isDisponivel = false
                    textDisponibilidade.text = "Não Disponível"
                    textDisponibilidade.setTextColor(Color.RED)
                }

                Toast.makeText(this, "Livro alugado com sucesso! 🎉", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Desculpe, este livro não está disponível no estoque no momento.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun configurarMarcadoresDeLeitura(livro: Livro) {
        val buttonNaoLido = findViewById<MaterialButton>(R.id.buttonNaoLido)
        val buttonLendo = findViewById<MaterialButton>(R.id.buttonLendo)
        val buttonLido = findViewById<MaterialButton>(R.id.buttonLido)

        // Usamos SharedPreferences para salvar o status localmente.
        // Usar o titulo ou um id do livro garante que cada um tenha seu próprio registro.
        val sharedPrefs = getSharedPreferences("BiblioUniforPrefs", Context.MODE_PRIVATE)
        val livroIdentificador = livro.id ?: livro.titulo ?: "desconhecido"
        val statusSalvo = sharedPrefs.getString("status_$livroIdentificador", "NAO_LIDO")

        // Aplica o visual correto baseado no que estava salvo
        atualizarVisualBotoesLeitura(statusSalvo, buttonNaoLido, buttonLendo, buttonLido)

        // Listeners para atualizar o estado ao clicar
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
        // Cores convertidas para ColorStateList (necessário para MaterialButton)
        val corSelecionado = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.biblio_blue))
        val corPadrao = ColorStateList.valueOf(Color.parseColor("#80415E5E")) // Cinza semi-transparente para o desmarcado

        // Reseta todos os botões para a cor padrão (apagados)
        btnNaoLido.backgroundTintList = corPadrao
        btnLendo.backgroundTintList = corPadrao
        btnLido.backgroundTintList = corPadrao

        // Acende apenas o botão do status atual do livro
        when (status) {
            "NAO_LIDO" -> btnNaoLido.backgroundTintList = corSelecionado
            "LENDO" -> btnLendo.backgroundTintList = corSelecionado
            "LIDO" -> btnLido.backgroundTintList = corSelecionado
        }
    }
}