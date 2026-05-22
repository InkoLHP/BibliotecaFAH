package com.example.bibliounifornew.utils

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object EmailSender {

    private val client = OkHttpClient()

    fun enviarEmail(
        email: String,
        codigo: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        // Log para acompanhar no Logcat o que está saindo do app
        println("DEBUG_EMAIL_JS: Enviando codigo $codigo para o email $email")

        val json = JSONObject()

        // 1. Parâmetros principais da Raiz (Apenas estes são permitidos aqui)
        json.put("service_id", "service_7tswcla")
        json.put("template_id", "template_vte7l0k")
        json.put("user_id", "vWHdFqVsAJz0IP4NC")
        json.put("accessToken", "LDBkD0Fz4RV1UIu82Z2Dx")

        // 2. Parâmetros do Template (Onde ficam as suas variáveis dinâmicas)
        val params = JSONObject()
        params.put("to_email", email) // Garanta que no site o campo "To Email" esteja {{to_email}}
        params.put("codigo", codigo)   // Garanta que no corpo do email esteja {{codigo}}

        json.put("template_params", params)

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

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
                val respostaDoServidor = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    // Imprime o erro real se o EmailJS recusar
                    println("ERRO_EMAIL_JS: Status Code: ${response.code} - Resposta: $respostaDoServidor")
                    onError()
                }
            }
        })
    }
}