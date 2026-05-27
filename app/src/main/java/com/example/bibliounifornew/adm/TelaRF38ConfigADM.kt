package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.login.TelaRF02Intermediaria
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.model.*

class TelaRF38ConfigADM : Fragment(R.layout.telarf38_config_adm) {

    private lateinit var olhoADMconfig: ImageView
    private lateinit var editSenhaADMconfig: EditText
    private lateinit var textUsuarioHeader: TextView
    private lateinit var editNomeAdm: EditText
    private lateinit var editUsuarioAdm: EditText
    private lateinit var imagePerfilUsuario: ImageView

    // 🌟 NOVO: Adicionado botão global para controle de clique repetido
    private var processandoSalvamento = false

    private var imagemSelecionadaUri: Uri? = null

    private val selecionarImagem =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imagemSelecionadaUri = uri
                imagePerfilUsuario.load(uri) {
                    crossfade(true)
                    placeholder(R.drawable.user_placeholder)
                    error(R.drawable.user_placeholder)
                    transformations(CircleCropTransformation())
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences(
            "user_session",
            Context.MODE_PRIVATE
        )

        val emailAdm = sharedPref.getString("USER_EMAIL", "") ?: ""

        // VIEWS
        olhoADMconfig = view.findViewById(R.id.iconOlhoSenhaAtual)
        editSenhaADMconfig = view.findViewById(R.id.editSenhaAtual)
        textUsuarioHeader = view.findViewById(R.id.textUsuario)
        editNomeAdm = view.findViewById(R.id.editNomeAdm)
        editUsuarioAdm = view.findViewById(R.id.editUsuarioAdm)
        imagePerfilUsuario = view.findViewById(R.id.imagePerfilUsuario)

        imagePerfilUsuario.setOnClickListener {
            selecionarImagem.launch("image/*")
        }

        textUsuarioHeader.text = emailAdm
        editNomeAdm.setText(sharedPref.getString("USER_NOME", ""))

        carregarDadosADM(emailAdm)

        // BOTÕES
        val btnRedefinirSenha = view.findViewById<MaterialButton>(R.id.btnRedefinirSenha)
        val btnApagarConta = view.findViewById<MaterialButton>(R.id.btnApagarConta)
        // 🌟 CORRIGIDO: Mapeando o botão de salvar que estava faltando!
        val btnSalvarADM = view.findViewById<MaterialButton>(R.id.btnSalvarADM)

        var senhaVisivel = false

        // MOSTRAR / ESCONDER SENHA
        olhoADMconfig.setOnClickListener {
            if (senhaVisivel) {
                editSenhaADMconfig.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoADMconfig.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false
            } else {
                editSenhaADMconfig.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoADMconfig.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }
            editSenhaADMconfig.setSelection(editSenhaADMconfig.text.length)
        }

        btnSalvarADM?.setOnClickListener {
            val novoNome = editNomeAdm.text.toString().trim()
            val novoUsuario = editUsuarioAdm.text.toString().trim()

            if (novoNome.isEmpty() || novoUsuario.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos antes de salvar!", Toast.LENGTH_SHORT).show()
            } else {
                salvarAlteracoesADM(emailAdm, novoNome, novoUsuario)
            }
        }

        // REDEFINIR SENHA
        btnRedefinirSenha?.setOnClickListener {
            val fragment = TelaRF39RedefinirADMInterno().apply {
                arguments = Bundle().apply {
                    putString("USER_EMAIL", emailAdm)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }

        // APAGAR CONTA
        btnApagarConta?.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.popup_apagar_conta)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val editSenha = dialog.findViewById<EditText>(R.id.editSenhaPopup)
            val textErro = dialog.findViewById<TextView>(R.id.textErroSenhaPopup)
            val btnConfirmar = dialog.findViewById<Button>(R.id.buttonConfirmarApagarConta)
            val iconOlho = dialog.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

            val senhaAdm = "123456"
            var senhaVisivelPopup = false

            iconOlho.setOnClickListener {
                if (senhaVisivelPopup) {
                    editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    iconOlho.setImageResource(R.drawable.ic_eye_closed)
                    senhaVisivelPopup = false
                } else {
                    editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    iconOlho.setImageResource(R.drawable.ic_eye_open)
                    senhaVisivelPopup = true
                }
                editSenha.setSelection(editSenha.text.length)
            }

            btnConfirmar.setOnClickListener {
                val senhaDigitada = editSenha.text.toString()
                if (senhaDigitada == senhaAdm) {
                    dialog.dismiss()
                    val intent = Intent(requireActivity(), TelaRF02Intermediaria::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    textErro.visibility = View.VISIBLE
                }
            }
            dialog.show()
        }
    }

    private fun carregarDadosADM(email: String) {
        if (email.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .select {
                            filter { eq("email", email) }
                        }
                        .decodeSingleOrNull<User>()
                }

                user?.let {
                    textUsuarioHeader.text = it.email
                    editNomeAdm.setText(it.nome)
                    editUsuarioAdm.setText(it.usuario)
                    editSenhaADMconfig.setText(it.senha)

                    if (!it.foto.isNullOrEmpty()) {
                        imagePerfilUsuario.load(it.foto) {
                            crossfade(true)
                            placeholder(R.drawable.user_placeholder)
                            transformations(CircleCropTransformation())
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun salvarAlteracoesADM(email: String, nome: String, usuario: String) {
        if (processandoSalvamento) return
        processandoSalvamento = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Atualiza o banco do Supabase baseado no e-mail do ADM
                    SupabaseConfig.client.postgrest["users"].update({
                        set("nome", nome)
                        set("usuario", usuario)
                    }) {
                        filter { eq("email", email) }
                    }
                }

                // Atualiza também os dados locais gravados no SharedPreferences
                val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                sharedPref.edit().apply {
                    putString("USER_NOME", nome)
                    apply()
                }

                Toast.makeText(requireContext(), "Alterações salvas com sucesso! 👍", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro de conexão ao salvar dados.", Toast.LENGTH_SHORT).show()
            } finally {
                processandoSalvamento = false
            }
        }
    }
}