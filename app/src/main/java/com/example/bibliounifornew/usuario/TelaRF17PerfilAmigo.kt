package com.example.bibliounifornew.usuario

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SupabaseConfig
import com.example.bibliounifornew.model.Notificacao
import com.example.bibliounifornew.model.Amigo // 🌟 Não esqueça de importar o modelo Amigo!
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF17PerfilAmigo : Fragment(R.layout.telarf17_5_perfil_amigo) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePerfilAmigo = view.findViewById<ImageView>(R.id.imagePerfilAmigo)
        val textEmailPerfilAmigo = view.findViewById<TextView>(R.id.textEmailPerfilAmigo)
        val textNomePerfilAmigo = view.findViewById<TextView>(R.id.textNomePerfilAmigo)
        val textUsuarioPerfilAmigo = view.findViewById<TextView>(R.id.textUsuarioPerfilAmigo)
        val textBioPerfilAmigo = view.findViewById<TextView>(R.id.textBioPerfilAmigo)
        val buttonAdicionarAmigo = view.findViewById<MaterialButton>(R.id.buttonAdicionarAmigoPerfil)

        val amigoNome = arguments?.getString("AMIGO_NOME") ?: "Sem Nome"
        val amigoEmail = arguments?.getString("AMIGO_EMAIL") ?: ""
        val amigoFoto = arguments?.getString("AMIGO_FOTO")
        val amigoBio = arguments?.getString("AMIGO_BIO")

        textNomePerfilAmigo.text = amigoNome
        textEmailPerfilAmigo.text = if (amigoEmail.isNotEmpty()) amigoEmail else "Sem E-mail"
        textUsuarioPerfilAmigo.text = if (amigoEmail.isNotEmpty()) amigoEmail.substringBefore("@") else "usuario"
        textBioPerfilAmigo.text = if (!amigoBio.isNullOrEmpty()) amigoBio else "Este usuário não informou uma biografia."

        if (!amigoFoto.isNullOrEmpty()) {
            imagePerfilAmigo.load(amigoFoto) {
                crossfade(true)
                placeholder(R.drawable.user_placeholder)
                error(R.drawable.user_placeholder)
            }
        } else {
            imagePerfilAmigo.setImageResource(R.drawable.user_placeholder)
        }

        // 🌟 VERIFICA NO BANCO SE JÁ SÃO AMIGOS ANTES DE DEIXAR CLICAR
        verificarSeJaEAmigo(amigoEmail, buttonAdicionarAmigo)

        buttonAdicionarAmigo.setOnClickListener {
            if (amigoEmail.isNotEmpty()) {
                enviarConviteAmizade(amigoEmail, amigoNome, buttonAdicionarAmigo)
            } else {
                Toast.makeText(requireContext(), "Erro: E-mail do usuário não encontrado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🌟 NOVA FUNÇÃO
    private fun verificarSeJaEAmigo(emailDoPerfil: String, botao: MaterialButton) {
        val sharedPref = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val meuEmail = sharedPref.getString("USER_EMAIL", "") ?: ""

        if (meuEmail.isEmpty() || emailDoPerfil.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val amizadeExiste = withContext(Dispatchers.IO) {
                    SupabaseConfig.client.postgrest["amigos"].select {
                        filter {
                            eq("usuario_email", meuEmail)
                            eq("amigo_email", emailDoPerfil)
                        }
                    }.decodeList<Amigo>()
                }

                // Se a lista não estiver vazia, significa que a amizade está no banco!
                if (amizadeExiste.isNotEmpty()) {
                    botao.text = "Vocês já são amigos"
                    botao.isEnabled = false // Desativa o clique
                    // O Android já deixa o botão cinza automaticamente quando isEnabled = false!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun enviarConviteAmizade(destinatarioEmail: String, destinatarioNome: String, botao: MaterialButton) {
        // ... (Mantenha o código de enviarConviteAmizade exatamente igual ao que fizemos antes)
    }
}