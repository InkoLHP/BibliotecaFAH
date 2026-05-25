package com.example.bibliounifornew.adm

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
import com.example.bibliounifornew.data.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF39RedefinirADMInterno : Fragment(R.layout.telarf39_redefinir_adm_interno) {

    private var emailAdm: String? = null
    private var senhaAntigaBanco: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Captura o e-mail passado via argumentos
        emailAdm = arguments?.getString("USER_EMAIL")

        // MAPEAMENTO DOS ELEMENTOS
        val textEmailADM = view.findViewById<TextView>(R.id.textEmailADM)
        val editNovaSenha = view.findViewById<EditText>(R.id.editNovaSenhaADM)
        val editConfirmarSenha = view.findViewById<EditText>(R.id.editConfirmarSenhaADM)
        val iconOlhoNovaSenha = view.findViewById<ImageView>(R.id.iconOlhoNovaSenhaADM)
        val iconOlhoConfirmar = view.findViewById<ImageView>(R.id.iconOlhoConfirmarADM)
        val btnSalvar = view.findViewById<MaterialButton>(R.id.buttonSalvarAlteracoesADM)

        // Textos de Erro mapeados
        val textErroNovaSenha = view.findViewById<TextView>(R.id.textErroNovaSenhaADM)
        val textErroSenhaAntiga = view.findViewById<TextView>(R.id.textErroSenhaAntigaADM)
        val textErroConfirmacao = view.findViewById<TextView>(R.id.textErroConfirmacaoADM)
        val textRegrasSenha = view.findViewById<TextView>(R.id.textRegrasSenhaADM)
        val textErroSenhas = view.findViewById<TextView>(R.id.textErroSenhasADM)

        // Esconde todos os erros no início
        ocultarErros(textErroNovaSenha, textErroSenhaAntiga, textErroConfirmacao, textRegrasSenha, textErroSenhas)

        // Preenche o cabeçalho com o e-mail logado
        textEmailADM.text = emailAdm ?: "adm@gmail.com"

        // Busca a senha atual no banco para validação futura
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

            if (!valido) return@setOnClickListener 

            // Validação de Força da Senha (Mínimo 8 chars, 1 Número, 1 Maiúscula)
            val regexForcaSenha = Regex("^(?=.*[A-Z])(?=.*[0-9]).{8,}\$")
            if (!regexForcaSenha.matches(novaSenha)) {
                textRegrasSenha.text = "A senha deve conter pelo menos 8 caracteres, um número e uma letra maiúscula!"
                textRegrasSenha.visibility = View.VISIBLE
                valido = false
            }

            // Verifica se é igual a senha anterior
            if (senhaAntigaBanco != null && novaSenha == senhaAntigaBanco) {
                textErroSenhaAntiga.text = "A senha deve ser diferente da anterior"
                textErroSenhaAntiga.visibility = View.VISIBLE
                valido = false
            }

            // Verifica se as senhas coincidem
            if (novaSenha != confirmarSenha) {
                textErroSenhas.text = "As senhas estão diferentes, verifique!"
                textErroSenhas.visibility = View.VISIBLE
                valido = false
            }

            // Salva a nova senha se passar em todos os testes
            if (valido) {
                if (emailAdm.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Erro: Identificação do ADM perdida.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                btnSalvar.isEnabled = false

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].update(
                                { set("senha", novaSenha) }
                            ) {
                                filter { eq("email", emailAdm!!) }
                            }
                        }

                        senhaAntigaBanco = novaSenha

                        val snackbar = Snackbar.make(view, "Alterações salvas com sucesso!", Snackbar.LENGTH_INDEFINITE)
                        snackbar.setAction("VOLTAR") {
                            parentFragmentManager.popBackStack()
                        }
                        snackbar.show()

                        btnSalvar.isEnabled = true
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

    private fun carregarSenhaAntiga() {
        if (emailAdm.isNullOrBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .select { filter { eq("email", emailAdm!!) } }
                        .decodeSingleOrNull<User>()
                }
                senhaAntigaBanco = user?.senha
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun configurarOlhoSenha(icone: ImageView, campoTexto: EditText) {
        var senhaVisivel = false
        icone.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                campoTexto.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                icone.setImageResource(R.drawable.ic_eye_open) 
            } else {
                campoTexto.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                icone.setImageResource(R.drawable.ic_eye_closed)
            }
            campoTexto.setSelection(campoTexto.text.length)
        }
    }

    private fun ocultarErros(vararg textViews: TextView) {
        for (tv in textViews) {
            tv.visibility = View.GONE
        }
    }
}
