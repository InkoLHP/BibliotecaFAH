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
import com.example.bibliounifornew.model.User
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF38ConfigADM : Fragment(R.layout.telarf38_config_adm) {

    // Componentes da tela
    private lateinit var editNomeAdm: EditText
    private lateinit var editUsuarioAdm: EditText
    private lateinit var editSenhaAtual: EditText
    private lateinit var textUsuarioHeader: TextView
    private lateinit var imagePerfilUsuario: ImageView
    private lateinit var btnSalvarADM: MaterialButton
    private lateinit var iconOlhoSenhaAtual: ImageView

    private var processandoSalvamento = false
    private var imagemSelecionadaUri: Uri? = null
    private var senhaVisivel = false

    // Launcher para abrir a galeria
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

        // Inicialização dos componentes
        vincularComponentes(view)

        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val emailAdm = sharedPref.getString("USER_EMAIL", "") ?: ""

        // Carrega dados iniciais
        carregarDadosADM(emailAdm)

        // Configura as ações dos botões e ícones
        configurarCliquesDeEdicao(view)
        configurarOlhoSenha()
        configurarBotoesAcao(emailAdm)
    }

    private fun vincularComponentes(view: View) {
        editNomeAdm = view.findViewById(R.id.editNomeAdm)
        editUsuarioAdm = view.findViewById(R.id.editUsuarioAdm)
        editSenhaAtual = view.findViewById(R.id.editSenhaAtual)
        textUsuarioHeader = view.findViewById(R.id.textUsuario)
        imagePerfilUsuario = view.findViewById(R.id.imagePerfilUsuario)
        btnSalvarADM = view.findViewById(R.id.btnSalvarADM)
        iconOlhoSenhaAtual = view.findViewById(R.id.iconOlhoSenhaAtual)
    }

    private fun configurarCliquesDeEdicao(view: View) {
        // Lápis do campo Nome
        view.findViewById<ImageView>(R.id.iconEditNomeAdm).setOnClickListener {
            editNomeAdm.isEnabled = !editNomeAdm.isEnabled
            if (editNomeAdm.isEnabled) editNomeAdm.requestFocus()
        }

        // Lápis do campo Usuário
        view.findViewById<ImageView>(R.id.iconEditUsuarioAdm).setOnClickListener {
            editUsuarioAdm.isEnabled = !editUsuarioAdm.isEnabled
            if (editUsuarioAdm.isEnabled) editUsuarioAdm.requestFocus()
        }

        // Lápis do Header
        view.findViewById<ImageView>(R.id.btnEditarUsuario).setOnClickListener {
            selecionarImagem.launch("image/*")
        }

        // Clique na foto também abre a galeria
        imagePerfilUsuario.setOnClickListener {
            selecionarImagem.launch("image/*")
        }
    }

    private fun configurarOlhoSenha() {
        iconOlhoSenhaAtual.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }
    }

    private fun configurarBotoesAcao(emailAdm: String) {
        btnSalvarADM.setOnClickListener {
            val novoNome = editNomeAdm.text.toString().trim()
            val novoUsuario = editUsuarioAdm.text.toString().trim()

            if (novoNome.isEmpty() || novoUsuario.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                salvarAlteracoesADM(emailAdm, novoNome, novoUsuario)
            }
        }

        view?.findViewById<MaterialButton>(R.id.btnRedefinirSenha)?.setOnClickListener {
            val fragment = TelaRF39RedefinirADMInterno().apply {
                arguments = Bundle().apply { putString("USER_EMAIL", emailAdm) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }

        view?.findViewById<MaterialButton>(R.id.btnApagarConta)?.setOnClickListener {
            mostrarDialogApagarConta()
        }
    }

    private fun carregarDadosADM(email: String) {
        if (email.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .select { filter { eq("email", email) } }
                        .decodeSingleOrNull<User>()
                }

                user?.let {
                    textUsuarioHeader.text = it.email
                    editNomeAdm.setText(it.nome)
                    editUsuarioAdm.setText(it.usuario)
                    editSenhaAtual.setText(it.senha)

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

        btnSalvarADM.text = "Salvando..."
        btnSalvarADM.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                var novaFotoUrl: String? = null

                if (imagemSelecionadaUri != null) {
                    val bytes = withContext(Dispatchers.IO) {
                        requireContext().contentResolver.openInputStream(imagemSelecionadaUri!!)?.readBytes()
                    }
                    if (bytes != null) {
                        val nomeArquivo = "perfil_${System.currentTimeMillis()}.jpg"
                        withContext(Dispatchers.IO) {
                            SupabaseConfig.client.storage.from("fotos_perfil").upload(nomeArquivo, bytes) {
                                upsert = true
                            }
                        }
                        novaFotoUrl = SupabaseConfig.client.storage.from("fotos_perfil").publicUrl(nomeArquivo)
                    }
                }

                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"].update(
                        update = {
                            set("nome", nome)
                            set("usuario", usuario)
                            if (novaFotoUrl != null) set("foto", novaFotoUrl)
                        }
                    ) {
                        filter { eq("email", email) }
                    }
                }

                // Atualiza SharedPreferences
                val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                sharedPref.edit().apply {
                    putString("USER_NOME", nome)
                    if (novaFotoUrl != null) putString("USER_FOTO", novaFotoUrl)
                    apply()
                }

                Toast.makeText(requireContext(), "Alterações salvas!", Toast.LENGTH_SHORT).show()
                imagemSelecionadaUri = null
                
                // Tranca campos novamente
                editNomeAdm.isEnabled = false
                editUsuarioAdm.isEnabled = false

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao salvar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                processandoSalvamento = false
                btnSalvarADM.text = "Salvar Alterações"
                btnSalvarADM.isEnabled = true
            }
        }
    }

    private fun mostrarDialogApagarConta() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.popup_apagar_conta)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editSenha = dialog.findViewById<EditText>(R.id.editSenhaPopup)
        val textErro = dialog.findViewById<TextView>(R.id.textErroSenhaPopup)
        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonConfirmarApagarConta)
        val iconOlho = dialog.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

        var senhaVisivelPopup = false

        iconOlho.setOnClickListener {
            senhaVisivelPopup = !senhaVisivelPopup
            if (senhaVisivelPopup) {
                editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlho.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlho.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenha.setSelection(editSenha.text.length)
        }

        btnConfirmar.setOnClickListener {
            val senhaDigitada = editSenha.text.toString()
            // Aqui você deve validar com a senha real do admin vinda do DB
            if (senhaDigitada == editSenhaAtual.text.toString()) {
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