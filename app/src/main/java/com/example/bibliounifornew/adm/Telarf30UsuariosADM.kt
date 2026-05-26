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
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Telarf30UsuariosADM : Fragment(R.layout.telarf30_usuarios_adm) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Elementos da Tela Principal
        val textNomeUsuario = view.findViewById<TextView>(R.id.textNomeUsuario)
        val textEmailUsuario = view.findViewById<TextView>(R.id.textEmailUsuario)
        val imageUsuario = view.findViewById<ImageView>(R.id.imageUsuario)

        val buttonSolicitacoes = view.findViewById<MaterialButton>(R.id.buttonSolicitacoes)
        val buttonLivrosAlugados = view.findViewById<MaterialButton>(R.id.buttonLivrosAlugados)
        val buttonAtrasos = view.findViewById<MaterialButton>(R.id.buttonAtrasos)
        val buttonPermissao = view.findViewById<MaterialButton>(R.id.buttonPermissao)
        val buttonExcluirConta = view.findViewById<MaterialButton>(R.id.buttonExcluirConta)

        // Resgatando dados vindos do Bundle (Tela 29)
        val nome = arguments?.getString("nome") ?: "Usuário"
        val email = arguments?.getString("email") ?: "Sem e-mail"
        val fotoUrl = arguments?.getString("foto")

        // Injetando dados na interface principal
        textNomeUsuario.text = nome
        textEmailUsuario.text = email
        Glide.with(this).load(fotoUrl).placeholder(R.drawable.user_placeholder).into(imageUsuario)

        // Mudança 1: Ir para a tela de solicitações global
        buttonSolicitacoes.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf31SolicitacoesADM())
                .addToBackStack(null)
                .commit()
        }

        // Mudança 2: Abre a tela de alugados PASSANDO os dados necessários do usuário
        buttonLivrosAlugados.setOnClickListener {
            val fragmentAlugados = Telarf30UsuarioAlugadosADM().apply {
                arguments = Bundle().apply {
                    putString("nome", nome)
                    putString("email", email)
                    putString("foto", fotoUrl)
                    putBoolean("apenasAtrasos", false) // Mostrar tudo
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragmentAlugados)
                .addToBackStack(null)
                .commit()
        }

        // Mudança 2.5: Abre a tela de alugados mostrando APENAS os atrasos dele
        buttonAtrasos.setOnClickListener {
            val fragmentAtrasos = Telarf30UsuarioAlugadosADM().apply {
                arguments = Bundle().apply {
                    putString("nome", nome)
                    putString("email", email)
                    putString("foto", fotoUrl)
                    putBoolean("apenasAtrasos", true) // Filtro de atrasos ligado!
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragmentAtrasos)
                .addToBackStack(null)
                .commit()
        }

        // POP-UP: MUDAR PERMISSÃO
        buttonPermissao.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.popup_mudar_permissao)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btnMudar = dialog.findViewById<MaterialButton>(R.id.buttonMudarParaAdm)
            val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPermissao)

            btnMudar.setOnClickListener {
                Toast.makeText(requireContext(), "$nome agora possui privilégios de ADM.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            btnCancelar.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }

        // Mudança 3: POP-UP REMOVER CONTA (Valida com a senha do ADM logado e apaga do Supabase)
        buttonExcluirConta.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.popup_apagar_conta)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val editSenha = dialog.findViewById<EditText>(R.id.editSenhaPopup)
            val iconOlho = dialog.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)
            val textErro = dialog.findViewById<TextView>(R.id.textErroSenhaPopup)
            val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)

            // 🔑 Busca a senha do ADM logado na sessão do app
            val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val senhaDoAdmLogado = sharedPref.getString("USER_SENHA", "") // Modifique o termo "USER_SENHA" caso tenha salvo com outra chave no login

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

                    // Conecta no Supabase e deleta o usuário baseado no e-mail dele
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                SupabaseConfig.client.from("users").delete {
                                    filter {
                                        eq("email", email)
                                    }
                                }
                            }
                            Toast.makeText(requireContext(), "Conta de $nome excluída definitivamente.", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                            parentFragmentManager.popBackStack() // Volta para a lista de gerenciamento
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
}