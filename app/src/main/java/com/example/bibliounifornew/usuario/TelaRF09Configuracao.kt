package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.example.bibliounifornew.model.User
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
// 🌟 IMPORTAÇÃO NOVA PARA O UPLOAD DA FOTO FUNCIONAR
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF09Configuracao : Fragment(R.layout.telarf09_configuracao) {

    private var emailUsuarioLogado: String? = null
    private var objetoUsuarioAtual: User? = null
    private lateinit var imagePerfilUsuario: ImageView

    // 🌟 VARIÁVEL: Guarda a foto escolhida até o usuário clicar em "Salvar"
    private var uriFotoTemporaria: Uri? = null

    private val selecionarImagem =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                uriFotoTemporaria = uri // Guarda a imagem na memória

                // Mostra a foto na tela pro usuário ver como ficou (Ilusão de ótica antes de salvar)
                imagePerfilUsuario.load(uri) {
                    crossfade(true)
                    placeholder(R.drawable.user_placeholder)
                    error(R.drawable.user_placeholder)
                }
            }
        }

    private lateinit var textEmailTop: TextView
    private lateinit var editNome: EditText
    private lateinit var editUsuario: EditText
    private lateinit var editBio: EditText
    private lateinit var editSenhaAtual: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        emailUsuarioLogado = arguments?.getString("USER_EMAIL") ?: sharedPref.getString("USER_EMAIL", "")

        // MAPEAMENTO DAS VIEWS
        textEmailTop = view.findViewById(R.id.textUsuario)
        editNome = view.findViewById(R.id.editNome)
        editUsuario = view.findViewById(R.id.editUsuario)
        editBio = view.findViewById(R.id.editBio)
        editSenhaAtual = view.findViewById(R.id.editSenhaAtual)
        imagePerfilUsuario = view.findViewById(R.id.imagePerfilUsuario)

        val btnEditarEmailTop = view.findViewById<ImageView>(R.id.btnEditarUsuario)
        val iconEditNome = view.findViewById<ImageView>(R.id.iconEditNome)
        val iconEditUsuario = view.findViewById<ImageView>(R.id.iconEditUsuario)
        val iconEditBio = view.findViewById<ImageView>(R.id.iconEditBio)
        val iconOlhoSenhaAtual = view.findViewById<ImageView>(R.id.iconOlhoSenhaAtual)

        val btnRedefinir = view.findViewById<MaterialButton>(R.id.buttonRedefinirSenha2)
        val btnApagar = view.findViewById<MaterialButton>(R.id.buttonApagarConta)
        val btnSalvarAlteracoes = view.findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        // Carrega foto da sessão se houver cache local
        val fotoSalva = sharedPref.getString("USER_FOTO", null)
        if (!fotoSalva.isNullOrBlank()) {
            imagePerfilUsuario.load(fotoSalva) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        }

        carregarDadosUsuario()

        imagePerfilUsuario.setOnClickListener {
            selecionarImagem.launch("image/*")
        }

        btnEditarEmailTop.setOnClickListener {
            abrirDialogEdicao("E-mail", textEmailTop.text.toString(), "email") { novoValor ->
                textEmailTop.text = novoValor
                emailUsuarioLogado = novoValor
            }
        }

        iconEditNome.setOnClickListener { liberarCampoEdicao(editNome) }
        iconEditUsuario.setOnClickListener { liberarCampoEdicao(editUsuario) }
        iconEditBio.setOnClickListener { liberarCampoEdicao(editBio) }

        var senhaPrincipalVisivel = false
        iconOlhoSenhaAtual.setOnClickListener {
            senhaPrincipalVisivel = !senhaPrincipalVisivel
            if (senhaPrincipalVisivel) {
                editSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }

        btnRedefinir.setOnClickListener {
            val fragment = TelaRF10RedefinirSenha().apply {
                arguments = Bundle().apply { putString("USER_EMAIL", emailUsuarioLogado) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }

        btnApagar.setOnClickListener {
            if (objetoUsuarioAtual != null) exibirPopupApagarConta()
            else Toast.makeText(requireContext(), "Aguardando sincronização...", Toast.LENGTH_SHORT).show()
        }

        // 🌟 ATUALIZADO: Agora sim o botão Salvar envia a FOTO REAL para a nuvem
        btnSalvarAlteracoes.setOnClickListener {
            // Trava os campos e o botão para evitar cliques duplos enquanto salva
            editNome.isEnabled = false
            editUsuario.isEnabled = false
            editBio.isEnabled = false
            btnSalvarAlteracoes.isEnabled = false
            btnSalvarAlteracoes.text = "Salvando..."

            viewLifecycleOwner.lifecycleScope.launch {
                // 1. Salva os textos normais
                salvarNoBanco("nome", editNome.text.toString().trim())
                salvarNoBanco("usuario", editUsuario.text.toString().trim())
                salvarNoBanco("bio", editBio.text.toString().trim())

                // 2. Se o usuário escolheu uma foto nova, envia para o Storage
                if (uriFotoTemporaria != null) {
                    val linkPublicoDaFoto = fazerUploadDaFoto(uriFotoTemporaria!!)

                    if (linkPublicoDaFoto != null) {
                        salvarNoBanco("foto", linkPublicoDaFoto) // Agora salva o link https!

                        // Salva no cache local do app
                        requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            .edit().putString("USER_FOTO", linkPublicoDaFoto).apply()

                        // Limpa a variável pois já foi salva
                        uriFotoTemporaria = null
                    } else {
                        Toast.makeText(requireContext(), "Erro ao enviar a foto para a nuvem. Tente novamente.", Toast.LENGTH_LONG).show()
                    }
                }

                Toast.makeText(requireContext(), "Alterações salvas com sucesso!", Toast.LENGTH_SHORT).show()
                // Destrava o botão
                btnSalvarAlteracoes.isEnabled = true
                btnSalvarAlteracoes.text = "Salvar Alterações"
            }
        }
    }

    private fun liberarCampoEdicao(editText: EditText) {
        editText.isEnabled = true
        editText.requestFocus()
        editText.setSelection(editText.text.length)

        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun carregarDadosUsuario() {
        if (emailUsuarioLogado.isNullOrBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .select { filter { eq("email", emailUsuarioLogado!!) } }
                        .decodeSingleOrNull<User>()
                }

                user?.let {
                    objetoUsuarioAtual = it
                    textEmailTop.text = it.email
                    editNome.setText(it.nome)
                    editUsuario.setText(it.usuario)
                    editBio.setText(it.bio ?: "")
                    editSenhaAtual.setText(it.senha)

                    val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                    val fotoSalva = sharedPref.getString("USER_FOTO", null)

                    if (fotoSalva.isNullOrBlank() && !it.foto.isNullOrBlank()) {
                        imagePerfilUsuario.load(it.foto)
                        sharedPref.edit { putString("USER_FOTO", it.foto) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun salvarNoBanco(coluna: String, novoValor: String) {
        if (emailUsuarioLogado.isNullOrBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .update(update = { set(coluna, novoValor) }) {
                            filter { eq("email", emailUsuarioLogado!!) }
                        }
                }

                val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                when (coluna) {
                    "nome" -> {
                        objetoUsuarioAtual?.nome = novoValor
                        sharedPref.edit { putString("USER_NOME", novoValor) }
                    }
                    "usuario" -> objetoUsuarioAtual?.usuario = novoValor
                    "bio" -> objetoUsuarioAtual?.bio = novoValor
                    "foto" -> {
                        objetoUsuarioAtual?.foto = novoValor
                        sharedPref.edit { putString("USER_FOTO", novoValor) }
                    }
                    "email" -> {
                        objetoUsuarioAtual?.email = novoValor
                        emailUsuarioLogado = novoValor
                        textEmailTop.text = novoValor
                        sharedPref.edit { putString("USER_EMAIL", novoValor) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro de conexão ao salvar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun abrirDialogEdicao(titulo: String, valorAtual: String, campoBanco: String, onSuccess: (String) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Editar $titulo")

        val input = EditText(requireContext())
        input.setText(valorAtual)
        builder.setView(input)

        builder.setPositiveButton("Salvar") { _, _ ->
            val novoValor = input.text.toString().trim()
            if (novoValor.isNotEmpty()) {
                salvarNoBanco(campoBanco, novoValor)
                onSuccess(novoValor)
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun exibirPopupApagarConta() {
        AlertDialog.Builder(requireContext())
            .setTitle("Apagar Conta")
            .setMessage("Tem certeza que deseja apagar sua conta? Esta ação é irreversível.")
            .setPositiveButton("Sim, Apagar") { _, _ ->
                apagarContaNoBanco()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun apagarContaNoBanco() {
        if (emailUsuarioLogado.isNullOrBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["users"]
                        .delete { filter { eq("email", emailUsuarioLogado!!) } }
                }

                // Limpa a sessão e volta para o login
                val sharedPref = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                Toast.makeText(requireContext(), "Conta excluída com sucesso.", Toast.LENGTH_SHORT).show()

                val intent = Intent(requireContext(), TelaRF01BemVindo::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao apagar conta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // =========================================================
    // 🌟 NOVA FUNÇÃO: Envia a imagem real para o Supabase Storage
    // =========================================================
    private suspend fun fazerUploadDaFoto(uri: Uri): String? {
        return try {
            withContext(Dispatchers.IO) {
                // Transforma a foto do celular em "Bytes" (dados brutos)
                val bytes = requireContext().contentResolver.openInputStream(uri)?.readBytes()

                if (bytes != null) {
                    // Cria um nome único para o arquivo usando a data/hora exata
                    val nomeDoArquivo = "perfil_${System.currentTimeMillis()}.jpg"

                    // Conecta na pasta "fotos_perfil" usando .from()
                    val bucket = SupabaseConfig.client.storage.from("fotos_perfil")

                    // Faz o upload de verdade usando o bloco de opções
                    bucket.upload(nomeDoArquivo, bytes) {
                        upsert = true
                    }

                    // Pede pro Supabase qual é o link público (https) dessa foto pra gente salvar
                    bucket.publicUrl(nomeDoArquivo)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}