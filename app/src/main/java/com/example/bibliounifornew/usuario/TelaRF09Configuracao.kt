package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.example.bibliounifornew.model.User
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF09Configuracao : Fragment(R.layout.telarf09_configuracao) {

    private var emailUsuarioLogado: String? = null
    private var objetoUsuarioAtual: User? = null

    // Declaração das Views
    private lateinit var textEmailTop: TextView
    private lateinit var editNome: EditText
    private lateinit var editUsuario: EditText
    private lateinit var editBio: EditText
    private lateinit var editSenhaAtual: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Captura o e-mail via Arguments (mochila do Fragment)
        emailUsuarioLogado = arguments?.getString("USER_EMAIL")

        // MAPEAMENTO DAS VIEWS (RF09.1 e RF09.2)
        textEmailTop = view.findViewById(R.id.textUsuario)
        editNome = view.findViewById(R.id.editNome)
        editUsuario = view.findViewById(R.id.editUsuario)
        editBio = view.findViewById(R.id.editBio)
        editSenhaAtual = view.findViewById(R.id.editSenhaAtual)

        val imagePerfilUsuario = view.findViewById<ImageView>(R.id.imagePerfilUsuario)
        val btnEditarEmailTop = view.findViewById<ImageView>(R.id.btnEditarUsuario)
        val iconEditNome = view.findViewById<ImageView>(R.id.iconEditNome)
        val iconEditUsuario = view.findViewById<ImageView>(R.id.iconEditUsuario)
        val iconEditBio = view.findViewById<ImageView>(R.id.iconEditBio)
        val iconOlhoSenhaAtual = view.findViewById<ImageView>(R.id.iconOlhoSenhaAtual)

        val btnRedefinir = view.findViewById<MaterialButton>(R.id.buttonRedefinirSenha2)
        val btnApagar = view.findViewById<MaterialButton>(R.id.buttonApagarConta)
        val btnSalvarAlteracoes = view.findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        // Busca informações atuais
        carregarDadosUsuario()

        // RF09.7 - Alterar Foto de Perfil
        imagePerfilUsuario.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir galeria para escolher foto...", Toast.LENGTH_SHORT).show()
            // Aqui futuramente você implementa a lógica do ImagePicker
        }

        // RF09.5 - Alterar E-mail (Lápis do topo)
        btnEditarEmailTop.setOnClickListener {
            abrirDialogEdicao("E-mail", textEmailTop.text.toString(), "email") { novoValor ->
                textEmailTop.text = novoValor
                emailUsuarioLogado = novoValor // Atualiza a variável de controle
            }
        }

        // RF09.3 - Alterar Nome
        iconEditNome.setOnClickListener {
            editNome.requestFocus()
        }

        // RF09.4 - Alterar Nome de Usuário
        iconEditUsuario.setOnClickListener {
            editUsuario.requestFocus()
        }

        // RF09.6 - Alterar Biografia
        iconEditBio.setOnClickListener {
            editBio.requestFocus()
        }

        // RF09.8 - Exibir/Ocultar Senha Atual na Tela Principal
        var senhaPrincipalVisivel = false
        iconOlhoSenhaAtual.setOnClickListener {
            senhaPrincipalVisivel = !senhaPrincipalVisivel
            if (senhaPrincipalVisivel) {
                editSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_open) // Pode trocar o ícone se tiver um ic_eye_closed
            } else {
                editSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_closed) // Ícone de senha oculta
            }
            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }

        // RF09.9 - Botão Redefinir Senha
        btnRedefinir.setOnClickListener {
            val intent = Intent(requireContext(), TelaRF10RedefinirSenha::class.java)
            intent.putExtra("USER_EMAIL", emailUsuarioLogado)
            startActivity(intent)
        }

        // RF09.10 - Pop-up Apagar Conta
        btnApagar.setOnClickListener {
            if (objetoUsuarioAtual != null) {
                exibirPopupApagarConta()
            } else {
                Toast.makeText(requireContext(), "Aguardando servidor...", Toast.LENGTH_SHORT).show()
            }
        }

        btnSalvarAlteracoes.setOnClickListener {

            salvarNoBanco("nome", editNome.text.toString().trim())

            salvarNoBanco("usuario", editUsuario.text.toString().trim())

            salvarNoBanco("bio", editBio.text.toString().trim())

            Toast.makeText(requireContext(), "Alterações salvas com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- CARREGAR DADOS NA INICIALIZAÇÃO ---
    private fun carregarDadosUsuario() {
        if (emailUsuarioLogado.isNullOrBlank()) return Toast.makeText(
            requireContext(),
            "Email recebido: $emailUsuarioLogado",
            Toast.LENGTH_LONG
        ).show()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .select {
                            filter {
                                eq("email", emailUsuarioLogado!!)
                            }
                        }
                        .decodeSingleOrNull<User>()
                }

                user?.let {
                    objetoUsuarioAtual = it

                    textEmailTop.text = it.email ?: ""

                    editNome.setText(it.nome ?: "")

                    editUsuario.setText(it.usuario ?: "")

                    editBio.setText(it.bio ?: "")

                    editSenhaAtual.setText(it.senha ?: "")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- FUNÇÃO PARA SALVAR ALTERAÇÕES DIRETAS (Nome, Usuário, Bio) ---
    private fun salvarNoBanco(
        coluna: String,
        novoValor: String
    ) {

        if (emailUsuarioLogado.isNullOrBlank()) return
        if (novoValor.isBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                withContext(Dispatchers.IO) {

                    SupabaseConfig.client.postgrest["users"]
                        .update(
                            {
                                set(coluna, novoValor)
                            }
                        ) {
                            filter {
                                eq("email", emailUsuarioLogado!!)
                            }
                        }
                }

                when (coluna) {

                    "nome" -> objetoUsuarioAtual?.nome = novoValor

                    "usuario" -> objetoUsuarioAtual?.usuario = novoValor

                    "bio" -> objetoUsuarioAtual?.bio = novoValor

                    "email" -> {
                        objetoUsuarioAtual?.email = novoValor
                        emailUsuarioLogado = novoValor
                        textEmailTop.text = novoValor
                    }
                }

                Toast.makeText(
                    requireContext(),
                    "Dados atualizados!",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(
                    requireContext(),
                    "Erro ao salvar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --- DIALOG PARA EDITAR CAMPOS SENSÍVEIS (Ex: E-mail) ---
    private fun abrirDialogEdicao(titulo: String, valorAtual: String, coluna: String, onSuccess: (String) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Editar $titulo")

        val input = EditText(requireContext())
        input.setText(valorAtual)
        builder.setView(input)

        builder.setPositiveButton("Salvar") { dialog, _ ->
            val novoValor = input.text.toString().trim()
            if (novoValor.isNotEmpty()) {
                salvarNoBanco(coluna, novoValor)
                onSuccess(novoValor)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // --- RF09.10 e RF09.11: POP-UP APAGAR CONTA ---
    private fun exibirPopupApagarConta() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_apagar_conta, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Deixa os cantos arredondados do XML funcionarem

        val editSenhaPopup = dialogView.findViewById<EditText>(R.id.editSenhaPopup)
        val textErroSenhaPopup = dialogView.findViewById<TextView>(R.id.textErroSenhaPopup)
        val buttonConfirmar = dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)
        val iconOlhoPopup = dialogView.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

        // Exibir/Ocultar Senha no Pop-up
        var senhaPopupVisivel = false
        iconOlhoPopup.setOnClickListener {
            senhaPopupVisivel = !senhaPopupVisivel
            if (senhaPopupVisivel) {
                editSenhaPopup.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                editSenhaPopup.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            editSenhaPopup.setSelection(editSenhaPopup.text.length)
        }

        buttonConfirmar.setOnClickListener {
            val senhaDigitada = editSenhaPopup.text.toString().trim()

            if (senhaDigitada == objetoUsuarioAtual?.senha) {
                textErroSenhaPopup.visibility = View.GONE
                buttonConfirmar.isEnabled = false

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.postgrest["users"].delete {
                                filter { eq("email", emailUsuarioLogado!!) }
                            }
                        }

                        alertDialog.dismiss()
                        Toast.makeText(requireContext(), "Conta excluída com sucesso!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(requireContext(), TelaRF01BemVindo::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Erro ao deletar conta", Toast.LENGTH_SHORT).show()
                        buttonConfirmar.isEnabled = true
                    }
                }
            } else {
                // RF09.11 - Validação da senha atual
                textErroSenhaPopup.text = "Digite sua senha atual!"
                textErroSenhaPopup.visibility = View.VISIBLE
            }
        }

        alertDialog.show()
    }
}