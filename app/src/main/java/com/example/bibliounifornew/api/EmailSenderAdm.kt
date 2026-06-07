package com.example.bibliounifornew.api

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object EmailSenderAdm {

    private val client = OkHttpClient()

    fun enviarEmailCredencial(
        email: String,
        credencial: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        println("DEBUG_EMAIL_JS: Enviando credencial ADM $credencial para o email $email")

        val json = JSONObject()
        json.put("service_id", "service_7tswcla") // Mesmo serviço

        // ⚠️ ATENÇÃO: Aqui você vai colar o ID do Template novo que você vai criar lá no site do EmailJS!
        json.put("template_id", "COLOQUE_AQUI_O_SEU_NOVO_TEMPLATE")

        json.put("user_id", "vWHdFqVsAJz0IP4NC")
        json.put("accessToken", "LDBkD0Fz4RV1UIu82Z2Dx")

        val params = JSONObject()
        params.put("to_email", email)
        params.put("credencial", credencial) // Enviando a variável {{credencial}} pro molde

        json.put("template_params", params)

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.emailjs.com/api/v1.0/email/send")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onError()
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) onSuccess() else onError()
            }
        })
    }
}