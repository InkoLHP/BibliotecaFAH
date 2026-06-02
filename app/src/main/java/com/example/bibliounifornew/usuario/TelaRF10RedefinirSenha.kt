package com.example.bibliounifornew.usuario

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*

class TelaRF10RedefinirSenha : Fragment(R.layout.telarf10_redefinir_senha) {

    private var emailUsuarioLogado: String? = null
    private var senhaAntigaBanco: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailUsuarioLogado = arguments?.getString("USER_EMAIL")

        val textEmailUsuario = view.findViewById<TextView>(R.id.textEmailUsuario)
        val editNovaSenha = view.findViewById<EditText>(R.id.editNovaSenha)
        val editConfirmarSenha = view.findViewById<EditText>(R.id.editConfirmarSenha)
        val iconOlhoNovaSenha = view.findViewById<ImageView>(R.id.iconOlhoNovaSenha)
        val iconOlhoConfirmar = view.findViewById<ImageView>(R.id.iconOlhoConfirmar)
        val btnSalvar = view.findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        val textErroNovaSenha = view.findViewById<TextView>(R.id.textErroNovaSenha)
        val textErroSenhaAntiga = view.findViewById<TextView>(R.id.textErroSenhaAntiga)
        val textErroConfirmacao = view.findViewById<TextView>(R.id.textErroConfirmacao)
        val textRegrasSenha = view.findViewById<TextView>(R.id.textRegrasSenha)
        val textErroSenhas = view.findViewById<TextView>(R.id.textErroSenhas)

        ocultarErros(textErroNovaSenha, textErroSenhaAntiga, textErroConfirmacao, textRegrasSenha, textErroSenhas)
        textEmailUsuario.text = emailUsuarioLogado
        carregarSenhaAntiga()

        configurarOlhoSenha(iconOlhoNovaSenha, editNovaSenha)
        configurarOlhoSenha(iconOlhoConfirmar, editConfirmarSenha)

        btnSalvar.setOnClickListener {
            ocultarErros(textErroNovaSenha, textErroSenhaAntiga, textErroConfirmacao, textRegrasSenha, textErroSenhas)

            val novaSenha = editNovaSenha.text.toString().trim()
            val confirmarSenha = editConfirmarSenha.text.toString().trim()
            var valido = true

            if (novaSenha.isEmpty()) {
                textErroNovaSenha.visibility = View.VISIBLE
                valido = false
            }
            if (confirmarSenha.isEmpty()) {
                textErroConfirmacao.visibility = View.VISIBLE
                valido = false
            }

            if (!valido) return@setOnClickListener

            val regexForcaSenha = Regex("^(?=.*[A-Z])(?=.*[0-9]).{8,}\$")
            if (!regexForcaSenha.matches(novaSenha)) {
                textRegrasSenha.text = "A senha deve conter pelo menos 8 caracteres, um número e uma letra maiúscula!"
                textRegrasSenha.visibility = View.VISIBLE
                valido = false
            }

            if (senhaAntigaBanco != null && novaSenha == senhaAntigaBanco) {
                textErroSenhaAntiga.text = "A senha deve ser diferente da anterior"
                textErroSenhaAntiga.visibility = View.VISIBLE
                valido = false
            }

            if (novaSenha != confirmarSenha) {
                textErroSenhas.text = "As senhas estão diferentes, verifique!"
                textErroSenhas.visibility = View.VISIBLE
                valido = false
            }

            if (valido) {
                if (emailUsuarioLogado.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Erro: Identificação do usuário perdida.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                btnSalvar.isEnabled = false

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].update(
                                { set("senha", novaSenha) }
                            ) {
                                filter { eq("email", emailUsuarioLogado!!) }
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

    private fun configurarOlhoSenha(icone: ImageView, campoTexto: EditText) {
        var senhaVisivel = false
        icone.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                campoTexto.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                icone.setImageResource(R.drawable.ic_eye_open)
            } else {
                campoTexto.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                icone.setImageResource(R.drawable.ic_eye_open)
            }
            campoTexto.setSelection(campoTexto.text.length)
        }
    }

    private fun ocultarErros(vararg textViews: TextView) {
        for (tv in textViews) { tv.visibility = View.GONE }
    }

    // 👇 NOVO: Atualiza a foto se ela existir nessa tela
    override fun onResume() {
        super.onResume()
        val sharedPref = requireActivity().getSharedPreferences("user_session", AppCompatActivity.MODE_PRIVATE)
        val fotoUsuarioUri = sharedPref.getString("USER_FOTO", null)

        // CORRIGIDO PARA O ID DO SEU XML
        val profileImage = view?.findViewById<ImageView>(R.id.imagePerfilRedefinir)

        if (profileImage != null && !fotoUsuarioUri.isNullOrBlank()) {
            Glide.with(this)
                .load(fotoUsuarioUri)
                .circleCrop()
                .into(profileImage)
        }
    }
}