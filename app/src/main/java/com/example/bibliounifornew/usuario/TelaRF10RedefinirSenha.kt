package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF10RedefinirSenha : Fragment(R.layout.telarf10_redefinir_senha) {

    private var emailUsuarioLogado: String? = null
    private var senhaAntigaBanco: String? = null // Necessário para validar o RF10.3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Captura o e-mail vindo da Tela de Configuração via Arguments
        emailUsuarioLogado = arguments?.getString("USER_EMAIL")

        // MAPEAMENTO DOS ELEMENTOS
        val textEmailUsuario = view.findViewById<TextView>(R.id.textEmailUsuario)
        val editNovaSenha = view.findViewById<EditText>(R.id.editNovaSenha)
        val editConfirmarSenha = view.findViewById<EditText>(R.id.editConfirmarSenha)
        val iconOlhoNovaSenha = view.findViewById<ImageView>(R.id.iconOlhoNovaSenha)
        val iconOlhoConfirmar = view.findViewById<ImageView>(R.id.iconOlhoConfirmar)
        val btnSalvar = view.findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        // Textos de Erro mapeados
        val textErroNovaSenha = view.findViewById<TextView>(R.id.textErroNovaSenha)
        val textErroSenhaAntiga = view.findViewById<TextView>(R.id.textErroSenhaAntiga)
        val textErroConfirmacao = view.findViewById<TextView>(R.id.textErroConfirmacao)
        val textRegrasSenha = view.findViewById<TextView>(R.id.textRegrasSenha)
        val textErroSenhas = view.findViewById<TextView>(R.id.textErroSenhas)

        // Esconde todos os erros no início
        ocultarErros(textErroNovaSenha, textErroSenhaAntiga, textErroConfirmacao, textRegrasSenha, textErroSenhas)

        // Preenche o cabeçalho com o e-mail logado
        textEmailUsuario.text = emailUsuarioLogado

        // Busca a senha atual no banco para validação futura (RF10.3)
        carregarSenhaAntiga()

        // LÓGICA DOS ÍCONES DE OLHO (Mostrar/Ocultar Senha)
        configurarOlhoSenha(iconOlhoNovaSenha, editNovaSenha)
        configurarOlhoSenha(iconOlhoConfirmar, editConfirmarSenha)

        // VALIDAÇÃO + SALVAR NO SUPABASE
        btnSalvar.setOnClickListener {
            // Esconde os erros para refazer a validação limpa
            ocultarErros(textErroNovaSenha, textErroSenhaAntiga, textErroConfirmacao, textRegrasSenha, textErroSenhas)

            val novaSenha = editNovaSenha.text.toString().trim()
            val confirmarSenha = editConfirmarSenha.text.toString().trim()
            var valido = true

            // Verifica se os campos estão vazios
            if (novaSenha.isEmpty()) {
                textErroNovaSenha.visibility = View.VISIBLE
                valido = false
            }
            if (confirmarSenha.isEmpty()) {
                textErroConfirmacao.visibility = View.VISIBLE
                valido = false
            }

            if (!valido) return@setOnClickListener // Para aqui se tiver vazio

            // RF10.4 - Validação de Força da Senha (Mínimo 8 chars, 1 Número, 1 Maiúscula)
            val regexForcaSenha = Regex("^(?=.*[A-Z])(?=.*[0-9]).{8,}\$")
            if (!regexForcaSenha.matches(novaSenha)) {
                textRegrasSenha.text = "A senha deve conter pelo menos 8 caracteres, um número e uma letra maiúscula!"
                textRegrasSenha.visibility = View.VISIBLE
                valido = false
            }

            // RF10.3 - Verifica se é igual a senha anterior
            if (senhaAntigaBanco != null && novaSenha == senhaAntigaBanco) {
                textErroSenhaAntiga.text = "A senha deve ser diferente da anterior"
                textErroSenhaAntiga.visibility = View.VISIBLE
                valido = false
            }

            // RF10.2 - Verifica se as senhas coincidem
            if (novaSenha != confirmarSenha) {
                textErroSenhas.text = "As senhas estão diferentes, verifique!"
                textErroSenhas.visibility = View.VISIBLE
                valido = false
            }

            // RF10.5 - Salva a nova senha se passar em todos os testes
            if (valido) {
                if (emailUsuarioLogado.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Erro: Identificação do usuário perdida.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                btnSalvar.isEnabled = false // Desativa para evitar duplo clique

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].update(
                                { set("senha", novaSenha) }
                            ) {
                                filter { eq("email", emailUsuarioLogado!!) }
                            }
                        }

                        // Atualiza a variável local para evitar bugs se ele tentar mudar de novo sem sair da tela
                        senhaAntigaBanco = novaSenha

                        // RF10.6 - Snackbar confirmando e com botão para voltar
                        val snackbar = Snackbar.make(view, "Alterações salvas com sucesso!", Snackbar.LENGTH_INDEFINITE)
                        snackbar.setAction("VOLTAR") {
                            // Volta para o Fragment de Configurações
                            parentFragmentManager.popBackStack()
                        }
                        snackbar.show()

                        // Opcional: Reativar o botão caso ele apenas dispense o snackbar
                        btnSalvar.isEnabled = true

                        // Limpa os campos após salvar
                        editNovaSenha.text.clear()
                        editConfirmarSenha.text.clear()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Erro ao atualizar senha no servidor", Toast.LENGTH_SHORT).show()
                        btnSalvar.isEnabled = true
                    }
                }
            }
        }
    }

    // Função para buscar a senha antiga no Supabase (Necessária para RF10.3)
    private fun carregarSenhaAntiga() {
        if (emailUsuarioLogado.isNullOrBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .select { filter { eq("email", emailUsuarioLogado!!) } }
                        .decodeSingleOrNull<User>()
                }
                senhaAntigaBanco = user?.senha
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Função para configurar o mostrar/ocultar senha
    private fun configurarOlhoSenha(icone: ImageView, campoTexto: EditText) {
        var senhaVisivel = false
        icone.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                campoTexto.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                icone.setImageResource(R.drawable.ic_eye_open) // Mude para seu ícone de olho fechado se tiver
            } else {
                campoTexto.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                icone.setImageResource(R.drawable.ic_eye_open)
            }
            campoTexto.setSelection(campoTexto.text.length) // Mantém o cursor no final
        }
    }

    // Função auxiliar para esconder textos de erro
    private fun ocultarErros(vararg textViews: TextView) {
        for (tv in textViews) {
            tv.visibility = View.GONE
        }
    }
}