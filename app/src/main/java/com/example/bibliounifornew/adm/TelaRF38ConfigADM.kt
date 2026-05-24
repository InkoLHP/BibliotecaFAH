package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.data.User
import com.example.bibliounifornew.login.TelaRF02Intermediaria
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MAPEAMENTO DAS INFORMAÇÕES DO ADM (Sessão)
        val sharedPref = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        val emailAdm = sharedPref.getString("USER_EMAIL", "") ?: ""

        // Adicionando 'view.' antes do findViewById
        olhoADMconfig = view.findViewById(R.id.iconOlhoSenhaAtual)
        editSenhaADMconfig = view.findViewById(R.id.editSenhaAtual)
        textUsuarioHeader = view.findViewById(R.id.textUsuario)
        editNomeAdm = view.findViewById(R.id.editNomeAdm)
        editUsuarioAdm = view.findViewById(R.id.editUsuarioAdm)
        imagePerfilUsuario = view.findViewById(R.id.imagePerfilUsuario)

        // Carrega dados iniciais da sessão
        textUsuarioHeader.text = emailAdm
        editNomeAdm.setText(sharedPref.getString("USER_NOME", ""))
        
        // Busca dados atualizados do Supabase
        carregarDadosADM(emailAdm)

        // Botões específicos da tela de configuração
        val btnRedefinirSenha = view.findViewById<MaterialButton>(R.id.btnRedefinirSenha)
        val btnApagarConta = view.findViewById<MaterialButton>(R.id.btnApagarConta)

        var senhaVisivel = false

        // Mostrar senha
        olhoADMconfig.setOnClickListener {
            if (senhaVisivel) {

                // ESCONDER SENHA
                editSenhaADMconfig.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                olhoADMconfig.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false

            } else {

                // MOSTRAR SENHA
                editSenhaADMconfig.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                olhoADMconfig.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }

            // Mantém cursor no final
            editSenhaADMconfig.setSelection(editSenhaADMconfig.text.length)
        }

        btnRedefinirSenha?.setOnClickListener {
            // Navega para a tela de redefinir senha passando o e-mail do ADM logado
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

        btnApagarConta?.setOnClickListener {

            // No Fragment, usamos requireContext() em vez de 'this' para o Dialog
            val dialog = Dialog(requireContext())

            dialog.setContentView(R.layout.popup_apagar_conta)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // CAMPOS DO POPUP (aqui continua usando dialog.findViewById, está certinho)
            val editSenha = dialog.findViewById<EditText>(R.id.editSenhaPopup)
            val textErro = dialog.findViewById<TextView>(R.id.textErroSenhaPopup)
            val btnConfirmar = dialog.findViewById<Button>(R.id.buttonConfirmarApagarConta)
            val iconOlho = dialog.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

            // SENHA MOCKADA DO ADM
            val senhaAdm = "123456"

            // MOSTRAR / OCULTAR SENHA NO POPUP (renomeado para não conflitar com a tela principal)
            var senhaVisivelPopup = false

            iconOlho.setOnClickListener {
                if (senhaVisivelPopup) {

                    // ESCONDER
                    editSenha.inputType =
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_PASSWORD

                    iconOlho.setImageResource(R.drawable.ic_eye_closed)
                    senhaVisivelPopup = false

                } else {

                    // MOSTRAR
                    editSenha.inputType =
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                    iconOlho.setImageResource(R.drawable.ic_eye_open)
                    senhaVisivelPopup = true
                }

                editSenha.setSelection(editSenha.text.length)
            }

            // CONFIRMAR
            btnConfirmar.setOnClickListener {
                val senhaDigitada = editSenha.text.toString()

                if (senhaDigitada == senhaAdm) {

                    // FECHA POPUP
                    dialog.dismiss()

                    // VOLTA PARA O INÍCIO DO APP (Limpando a pilha de telas)
                    // Para chamar uma Activity a partir de um Fragment, usamos requireActivity()
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

                    // Carrega a foto de perfil usando Coil
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
}
