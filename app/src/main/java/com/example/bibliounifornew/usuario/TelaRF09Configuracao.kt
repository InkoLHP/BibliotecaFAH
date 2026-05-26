package com.example.bibliounifornew.usuario

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.example.bibliounifornew.data.User
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit

class TelaRF09Configuracao : Fragment(R.layout.telarf09_configuracao) {

    private var emailUsuarioLogado: String? = null
    private var objetoUsuarioAtual: User? = null

    // FOTO DE PERFIL
    private var imagemSelecionadaUri: Uri? = null

    private val selecionarImagem =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null && view != null) {
                imagemSelecionadaUri = uri
                view?.findViewById<ImageView>(R.id.imagePerfilUsuario)?.setImageURI(uri)

                val sharedPref = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                sharedPref.edit().putString("USER_FOTO", uri.toString()).apply()

                Toast.makeText(requireContext(), "Foto atualizada!", Toast.LENGTH_SHORT).show()
            }
        }

    // Declaração das Views
    private lateinit var textEmailTop: TextView
    private lateinit var editNome: EditText
    private lateinit var editUsuario: EditText
    private lateinit var editBio: EditText
    private lateinit var editSenhaAtual: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Captura o e-mail via Arguments
        emailUsuarioLogado = arguments?.getString("USER_EMAIL")

        // MAPEAMENTO DAS VIEWS
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

        // FOTO DE PERFIL
        imagePerfilUsuario.setOnClickListener {
            selecionarImagem.launch("image/*")
        }

        // Alterar E-mail
        btnEditarEmailTop.setOnClickListener {
            abrirDialogEdicao(
                "E-mail",
                textEmailTop.text.toString(),
                "email"
            ) { novoValor ->
                textEmailTop.text = novoValor
                emailUsuarioLogado = novoValor
            }
        }

        // Alterar Nome
        iconEditNome.setOnClickListener {
            editNome.requestFocus()
        }

        // Alterar Nome de Usuário
        iconEditUsuario.setOnClickListener {
            editUsuario.requestFocus()
        }

        // Alterar Biografia
        iconEditBio.setOnClickListener {
            editBio.requestFocus()
        }

        // Exibir/Ocultar Senha Atual
        var senhaPrincipalVisivel = false

        iconOlhoSenhaAtual.setOnClickListener {

            senhaPrincipalVisivel = !senhaPrincipalVisivel

            if (senhaPrincipalVisivel) {

                editSenhaAtual.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_open)

            } else {

                editSenhaAtual.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_closed)
            }

            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }

        // Botão Redefinir Senha
        btnRedefinir.setOnClickListener {

            val fragment = TelaRF10RedefinirSenha().apply {
                arguments = Bundle().apply {
                    putString("USER_EMAIL", emailUsuarioLogado)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Botão Apagar Conta
        btnApagar.setOnClickListener {

            if (objetoUsuarioAtual != null) {

                exibirPopupApagarConta()

            } else {

                Toast.makeText(
                    requireContext(),
                    "Aguardando servidor...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnSalvarAlteracoes.setOnClickListener {

            val sharedPref = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
            val fotoSalva = sharedPref.getString("USER_FOTO", null)

            if (fotoSalva != null) {
                try {
                    imagePerfilUsuario.setImageURI(Uri.parse(fotoSalva))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            salvarNoBanco("nome", editNome.text.toString().trim())

            salvarNoBanco("usuario", editUsuario.text.toString().trim())

            salvarNoBanco("bio", editBio.text.toString().trim())

            Toast.makeText(
                requireContext(),
                "Alterações salvas com sucesso!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // CARREGAR DADOS
    private fun carregarDadosUsuario() {

        if (emailUsuarioLogado.isNullOrBlank()) {

            Toast.makeText(
                requireContext(),
                "Email recebido: $emailUsuarioLogado",
                Toast.LENGTH_LONG
            ).show()

            return
        }

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

    // SALVAR ALTERAÇÕES
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
                    "nome" -> {
                        objetoUsuarioAtual?.nome = novoValor
                        requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                            .edit { putString("USER_NOME", novoValor) }
                    }
                    "usuario" -> objetoUsuarioAtual?.usuario = novoValor
                    "bio" -> objetoUsuarioAtual?.bio = novoValor
                    "email" -> {
                        objetoUsuarioAtual?.email = novoValor
                        emailUsuarioLogado = novoValor
                        textEmailTop.text = novoValor
                        // 🌟 Atualiza o e-mail na sessão global
                        requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                            .edit { putString("USER_EMAIL", novoValor) }
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

    // DIALOG EDITAR
    private fun abrirDialogEdicao(
        titulo: String,
        valorAtual: String,
        coluna: String,
        onSuccess: (String) -> Unit
    ) {

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

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    // POP-UP APAGAR CONTA
    private fun exibirPopupApagarConta() {

        val dialogView = LayoutInflater
            .from(requireContext())
            .inflate(R.layout.popup_apagar_conta, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editSenhaPopup =
            dialogView.findViewById<EditText>(R.id.editSenhaPopup)

        val textErroSenhaPopup =
            dialogView.findViewById<TextView>(R.id.textErroSenhaPopup)

        val buttonConfirmar =
            dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)

        val iconOlhoPopup =
            dialogView.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

        var senhaPopupVisivel = false

        iconOlhoPopup.setOnClickListener {

            senhaPopupVisivel = !senhaPopupVisivel

            if (senhaPopupVisivel) {

                editSenhaPopup.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            } else {

                editSenhaPopup.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
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

                            SupabaseConfig.client.postgrest["users"]
                                .delete {
                                    filter {
                                        eq("email", emailUsuarioLogado!!)
                                    }
                                }
                        }

                        alertDialog.dismiss()

                        Toast.makeText(
                            requireContext(),
                            "Conta excluída com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent =
                            Intent(requireContext(), TelaRF01BemVindo::class.java)

                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)

                        requireActivity().finish()

                    } catch (e: Exception) {

                        e.printStackTrace()

                        Toast.makeText(
                            requireContext(),
                            "Erro ao deletar conta",
                            Toast.LENGTH_SHORT
                        ).show()

                        buttonConfirmar.isEnabled = true
                    }
                }

            } else {

                textErroSenhaPopup.text = "Digite sua senha atual!"

                textErroSenhaPopup.visibility = View.VISIBLE
            }
        }

        alertDialog.show()
    }
}