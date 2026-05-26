package com.example.bibliounifornew.adm

import android.app.Dialog
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
import com.bumptech.glide.Glide
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

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

        // Resgatando dados vindos do Bundle
        val nome = arguments?.getString("nome") ?: "Usuário"
        val email = arguments?.getString("email") ?: "Sem e-mail"
        val fotoUrl = arguments?.getString("foto")
        val senhaCorreta = arguments?.getString("senhaCorreta") ?: ""

        // Injetando dados na interface principal
        textNomeUsuario.text = nome
        textEmailUsuario.text = email
        Glide.with(this).load(fotoUrl).placeholder(R.drawable.user_placeholder).into(imageUsuario)

        // Navegação simples para Livros Alugados
        buttonLivrosAlugados.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Telarf30UsuarioAlugadosADM())
                .addToBackStack(null)
                .commit()
        }

        // Pop-up Simples de Solicitações
        buttonSolicitacoes.setOnClickListener {
            Toast.makeText(requireContext(), "Solicitações de $nome carregadas.", Toast.LENGTH_SHORT).show()
        }

        // POP-UP: ATRASOS DE ALUGUEL (Design customizado da imagem)
        buttonAtrasos.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.popup_atrasos_aluguel)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btnFechar = dialog.findViewById<MaterialButton>(R.id.buttonFecharAtrasos)
            btnFechar.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }

        // POP-UP: MUDAR PERMISSÃO (Design customizado da imagem)
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

        // POP-UP: REMOVER CONTA (Usando o seu XML customizado completo)
        buttonExcluirConta.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.popup_apagar_conta) // Certifique-se de que salvou o seu xml com este nome
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val editSenha = dialog.findViewById<EditText>(R.id.editSenhaPopup)
            val iconOlho = dialog.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)
            val textErro = dialog.findViewById<TextView>(R.id.textErroSenhaPopup)
            val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)

            var senhaVisivel = false

            // Lógica de mostrar/ocultar senha (Ícone do Olho)
            iconOlho.setOnClickListener {
                if (senhaVisivel) {
                    editSenha.transformationMethod = PasswordTransformationMethod.getInstance()
                    iconOlho.setImageResource(R.drawable.ic_eye_open)
                } else {
                    editSenha.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    iconOlho.setImageResource(R.drawable.ic_eye_closed) // Mude para o ícone de olho fechado se tiver
                }
                senhaVisivel = !senhaVisivel
                editSenha.setSelection(editSenha.text.length) // Mantém o cursor no final do texto
            }

            // Validação e exclusão
            btnConfirmar.setOnClickListener {
                val senhaDigitada = editSenha.text.toString()

                if (senhaDigitada == senhaCorreta) {
                    textErro.visibility = View.GONE
                    Toast.makeText(requireContext(), "Conta de $nome excluída definitivamente.", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                    parentFragmentManager.popBackStack() // Retorna para a lista de usuários automaticamente
                } else {
                    textErro.visibility = View.VISIBLE // Ativa o aviso vermelho de erro estruturado no seu XML
                }
            }

            dialog.show()
        }
    }
}