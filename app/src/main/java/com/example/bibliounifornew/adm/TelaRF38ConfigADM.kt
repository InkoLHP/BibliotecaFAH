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

    private lateinit var olhoADMconfig: ImageView
    private lateinit var editSenhaADMconfig: EditText
    private lateinit var textUsuarioHeader: TextView
    private lateinit var editNomeAdm: EditText
    private lateinit var editUsuarioAdm: EditText
    private lateinit var imagePerfilUsuario: ImageView
    private lateinit var btnSalvarADM: MaterialButton // Declarado globalmente para podermos mudar o texto dele

    private var processandoSalvamento = false
    private var imagemSelecionadaUri: Uri? = null

    // Launcher para abrir a galeria
    private val selecionarImagem =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imagemSelecionadaUri = uri // Guarda a imagem escolhida
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

        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val emailAdm = sharedPref.getString("USER_EMAIL", "") ?: ""

        // INICIALIZANDO COMPONENTES VISUAIS
        olhoADMconfig = view.findViewById(R.id.iconOlhoSenhaAtual)
        editSenhaADMconfig = view.findViewById(R.id.editSenhaAtual)
        textUsuarioHeader = view.findViewById(R.id.textUsuario)
        editNomeAdm = view.findViewById(R.id.editNomeAdm)
        editUsuarioAdm = view.findViewById(R.id.editUsuarioAdm)
        imagePerfilUsuario = view.findViewById(R.id.imagePerfilUsuario)
        btnSalvarADM = view.findViewById(R.id.btnSalvarADM)

        // Clique na foto para abrir a galeria
        imagePerfilUsuario.setOnClickListener {
            selecionarImagem.launch("image/*")
        }

        textUsuarioHeader.text = emailAdm
        editNomeAdm.setText(sharedPref.getString("USER_NOME", ""))

        // Busca dados iniciais
        carregarDadosADM(emailAdm)

        // BOTÕES
        val btnRedefinirSenha = view.findViewById<MaterialButton>(R.id.btnRedefinirSenha)
        val btnApagarConta = view.findViewById<MaterialButton>(R.id.btnApagarConta)

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

        // AÇÃO DE SALVAR
        btnSalvarADM.setOnClickListener {
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
                arguments = Bundle().apply { putString("USER_EMAIL", emailAdm) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }

        // APAGAR CONTA (MANTIDO INTACTO)
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
                        .select { filter { eq("email", email) } }
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

        // Dá um feedback pro usuário que está carregando
        btnSalvarADM.text = "Salvando..."
        btnSalvarADM.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                var novaFotoUrl: String? = null

                // 1. SE TIVER FOTO SELECIONADA, FAZ UPLOAD PRO SUPABASE STORAGE
                if (imagemSelecionadaUri != null) {
                    val bytes = withContext(Dispatchers.IO) {
                        requireContext().contentResolver.openInputStream(imagemSelecionadaUri!!)?.readBytes()
                    }
                    if (bytes != null) {
                        val nomeArquivo = "perfil_${System.currentTimeMillis()}.jpg"

                        withContext(Dispatchers.IO) {
                            // Envia para o bucket "fotos_perfil"
                            SupabaseConfig.client.storage.from("fotos_perfil").upload(nomeArquivo, bytes)
                        }

                        // Pega o link público gerado
                        novaFotoUrl = SupabaseConfig.client.storage.from("fotos_perfil").publicUrl(nomeArquivo)
                    }
                }

                // 2. ATUALIZA A TABELA USERS NO BANCO DE DADOS
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"].update(
                        update = {
                            set("nome", nome)
                            set("usuario", usuario)
                            // Só atualiza a foto se o usuário realmente enviou uma foto nova
                            if (novaFotoUrl != null) {
                                set("foto", novaFotoUrl)
                            }
                        }
                    ) {
                        filter { eq("email", email) }
                    }
                }

                // 3. ATUALIZA A MEMÓRIA DO APLICATIVO
                val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                sharedPref.edit().apply {
                    putString("USER_NOME", nome)
                    if (novaFotoUrl != null) {
                        putString("USER_FOTO", novaFotoUrl) // Atualiza sessão se tiver header noutro lugar
                    }
                    apply()
                }

                Toast.makeText(requireContext(), "Alterações salvas com sucesso! 👍", Toast.LENGTH_SHORT).show()
                imagemSelecionadaUri = null // Reseta a imagem pra evitar upload duplo sem querer

            } catch (e: Exception) {
                e.printStackTrace()
                // Isso vai exibir o motivo real enviado pelo Supabase na tela do seu celular
                Toast.makeText(requireContext(), "Erro real: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                // Devolve o botão ao normal
                processandoSalvamento = false
                btnSalvarADM.text = "Salvar alterações"
                btnSalvarADM.isEnabled = true
            }
        }
    }
}