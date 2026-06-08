package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
// 🌟 IMPORTANTE: Usando Coil para padronizar com o resto do app
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
// Voltando para o EmailSenderAdm correto
import com.example.bibliounifornew.api.EmailSenderAdm

class Telarf30UsuariosADM : Fragment(R.layout.telarf30_usuarios_adm) {

    // Transformamos em variáveis da classe para que os botões usem os dados atualizados do banco
    private var nome: String = "Usuário"
    private var email: String = "Sem e-mail"
    private var fotoUrl: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Elementos da Tela Principal
        val textNomeUsuario = view.findViewById<TextView>(R.id.textNomeUsuario)
        val textEmailUsuario = view.findViewById<TextView>(R.id.textEmailUsuario)
        val imageUsuario = view.findViewById<ImageView>(R.id.imageUsuarioAmigos)

        val buttonSolicitacoes = view.findViewById<MaterialButton>(R.id.buttonSolicitacoes)
        val buttonLivrosAlugados = view.findViewById<MaterialButton>(R.id.buttonLivrosAlugados)
        val buttonAtrasos = view.findViewById<MaterialButton>(R.id.buttonAtrasos)
        val buttonPermissao = view.findViewById<MaterialButton>(R.id.buttonPermissao)
        val buttonExcluirConta = view.findViewById<MaterialButton>(R.id.buttonExcluirConta)

        // Descobre qual parâmetro de e-mail foi enviado para unificar a busca
        val emailRecebido = arguments?.getString("USER_EMAIL") ?: arguments?.getString("email")

        if (emailRecebido != null) {
            email = emailRecebido

            // SE COUBE APENAS O EMAIL (Vindo da Tela 36 de Aluguéis)
            if (arguments?.containsKey("USER_EMAIL") == true) {
                textNomeUsuario.text = "Carregando..."
                textEmailUsuario.text = email

                // Busca as informações restantes (nome e foto) no banco de dados
                buscarDadosDoUsuario(email) { nomeBanco: String, fotoBanco: String? ->
                    nome = nomeBanco
                    fotoUrl = fotoBanco

                    textNomeUsuario.text = nome
                    carregarFotoPerfil(imageUsuario, fotoUrl)
                }
            } else {
                // SE JÁ VEIO TUDO COMPLETO (Fluxo tradicional da Tela 29)
                nome = arguments?.getString("nome") ?: "Usuário"
                fotoUrl = arguments?.getString("foto")

                textNomeUsuario.text = nome
                textEmailUsuario.text = email
                carregarFotoPerfil(imageUsuario, fotoUrl) // 🌟 Chamando a função unificada
            }
        }

        // 1. NAVEGAÇÃO: TELA DE SOLICITAÇÕES
        buttonSolicitacoes.setOnClickListener {
            val fragmentSolicitacoes = Telarf31SolicitacoesADM().apply {
                arguments = Bundle().apply {
                    putString("EMAIL_FILTRO_ADM", email) // 🌟 Garante o preenchimento automático na busca
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragmentSolicitacoes)
                .addToBackStack(null)
                .commit()
        }

        // 2. NAVEGAÇÃO: TELA COMPLETA DE LIVROS ALUGADOS
        buttonLivrosAlugados.setOnClickListener {
            val fragmentAlugados = Telarf30UsuarioAlugadosADM().apply {
                arguments = Bundle().apply {
                    putString("nome", nome)
                    putString("email", email)
                    putString("foto", fotoUrl)
                    putBoolean("apenasAtrasos", false)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragmentAlugados)
                .addToBackStack(null)
                .commit()
        }

        // 3. POP-UP INTELIGENTE: ATRASOS COM CÁLCULO DE MULTA
        buttonAtrasos.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.popup_atrasos_aluguel)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btnFechar = dialog.findViewById<MaterialButton>(R.id.buttonFecharAtrasos)
            val textResultado = dialog.findViewById<TextView>(R.id.textResultadoAtrasos)

            if (textResultado != null) {
                textResultado.text = "Calculando multas no sistema..."
            }

            dialog.show()

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val todosAlugueis = withContext(Dispatchers.IO) {
                        SupabaseConfig.client.from("alugueis")
                            .select { filter { eq("email_usuario", email) } }
                            .decodeList<com.example.bibliounifornew.model.Aluguel>()
                    }

                    val atrasados = todosAlugueis.filter { it.dias_restantes != null && it.dias_restantes < 0 && it.devolvido == false }

                    if (atrasados.isEmpty()) {
                        textResultado?.text = "Tudo em dia!\n\n$nome não possui livros atrasados e não tem multas pendentes."
                    } else {
                        var totalMulta = 0.0
                        val valorMultaPorDia = 2.00
                        val relatorio = java.lang.StringBuilder()

                        relatorio.append("Livros em atraso:\n\n")

                        atrasados.forEach { livro ->
                            val diasDeAtraso = kotlin.math.abs(livro.dias_restantes!!)
                            val multaDoLivro = diasDeAtraso * valorMultaPorDia
                            totalMulta += multaDoLivro

                            val titulo = livro.titulo_livro
                            relatorio.append("• $titulo\n   Atraso: $diasDeAtraso dias | Multa: R$ ${String.format("%.2f", multaDoLivro)}\n\n")
                        }

                        relatorio.append("💰 MULTA TOTAL A COBRAR: R$ ${String.format("%.2f", totalMulta)}")
                        textResultado?.text = relatorio.toString()
                    }

                } catch (e: Exception) {
                    textResultado?.text = "Erro ao buscar dados do servidor."
                    e.printStackTrace()
                }
            }

            btnFechar?.setOnClickListener { dialog.dismiss() }
        }

        // 4. POP-UP: MUDAR PERMISSÃO
        buttonPermissao.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.popup_mudar_permissao)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btnMudar = dialog.findViewById<MaterialButton>(R.id.buttonMudarParaAdm)
            val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPermissao)

            btnMudar.setOnClickListener {
                // 1. Gera uma credencial aleatória de 8 números (ex: 22112006, 93847582)
                val novaCredencial = (10000000..99999999).random().toString()

                // Desabilita o botão para o administrador não clicar duas vezes sem querer
                btnMudar.isEnabled = false
                btnMudar.text = "Processando..."

                // 2. Abre a conexão com o banco de dados
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.from("users").update(
                                {
                                    set("tipo", "adm") // Atualiza para Administrador
                                    set("credencial", novaCredencial) // Salva a senha de 8 dígitos
                                }
                            ) {
                                filter { eq("email", email) } // Apenas para o e-mail que estamos vendo
                            }
                        }

                        // 3. Dispara o E-mail usando o EmailSenderAdm correto
                        EmailSenderAdm.enviarEmailCredencial(
                            email = email,
                            credencial = novaCredencial,
                            onSuccess = {
                                requireActivity().runOnUiThread {
                                    Toast.makeText(requireContext(), "$nome agora é ADM! A credencial foi enviada por e-mail.", Toast.LENGTH_LONG).show()
                                    dialog.dismiss()
                                }
                            },
                            onError = {
                                requireActivity().runOnUiThread {
                                    Toast.makeText(requireContext(), "$nome agora é ADM, MAS houve uma falha ao enviar o e-mail.", Toast.LENGTH_LONG).show()
                                    dialog.dismiss()
                                }
                            }
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Erro ao conectar com o banco de dados.", Toast.LENGTH_SHORT).show()
                        btnMudar.isEnabled = true
                        btnMudar.text = "Sim, Mudar para ADM"
                    }
                }
            }
            btnCancelar.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }

        // 5. POP-UP: REMOVER CONTA
        buttonExcluirConta.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.popup_apagar_conta)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val editSenha = dialog.findViewById<EditText>(R.id.editSenhaPopup)
            val iconOlho = dialog.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)
            val textErro = dialog.findViewById<TextView>(R.id.textErroSenhaPopup)
            val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)

            val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val senhaDoAdmLogado = sharedPref.getString("USER_SENHA", "")

            var senhaVisivel = false

            iconOlho.setOnClickListener {
                if (senhaVisivel) {
                    editSenha.transformationMethod = PasswordTransformationMethod.getInstance()
                    iconOlho.setImageResource(R.drawable.ic_eye_open)
                } else {
                    editSenha.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    iconOlho.setImageResource(R.drawable.ic_eye_closed)
                }
                senhaVisivel = !senhaVisivel
                editSenha.setSelection(editSenha.text.length)
            }

            btnConfirmar.setOnClickListener {
                val senhaDigitada = editSenha.text.toString()

                if (senhaDigitada == senhaDoAdmLogado && senhaDoAdmLogado.isNotEmpty()) {
                    textErro.visibility = View.GONE

                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                SupabaseConfig.client.from("users").delete {
                                    filter { eq("email", email) }
                                }
                            }
                            Toast.makeText(requireContext(), "Conta de $nome excluída definitivamente.", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                            parentFragmentManager.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Erro ao deletar do banco: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    textErro.visibility = View.VISIBLE
                }
            }

            dialog.show()
        }
    }

    // 🌟 NOVA FUNÇÃO: Isola a responsabilidade de carregar a imagem com segurança (Coil)
    private fun carregarFotoPerfil(imageView: ImageView, urlDaFoto: String?) {
        if (!urlDaFoto.isNullOrEmpty()) {
            imageView.load(urlDaFoto) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        } else {
            imageView.setImageResource(R.drawable.user_placeholder)
        }
    }

    private fun buscarDadosDoUsuario(email: String, onResult: (String, String?) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val usuario = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.from("users")
                        .select {
                            filter {
                                eq("email", email)
                            }
                        }.decodeSingleOrNull<com.example.bibliounifornew.model.User>()
                }

                if (usuario != null) {
                    onResult(usuario.nome, usuario.foto)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}