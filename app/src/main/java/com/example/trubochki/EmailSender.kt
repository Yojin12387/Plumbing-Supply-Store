package com.example.trubochki.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

object EmailSender {
    // ВАЖНО: Замените эти данные на свои из EmailJS
    private const val SERVICE_ID = "service_abc123"      // Ваш Service ID
    private const val TEMPLATE_ID = "template_xyz789"    // Ваш Template ID
    private const val PUBLIC_KEY = "user_abc123xyz"      // Ваш Public Key

    private val client = OkHttpClient()
    private val gson = Gson()

    fun sendCallback(
        context: Context,
        product: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val templateParams = mapOf(
            "product" to product,
            "phone" to phone,
            "date" to android.text.format.DateFormat.format("dd.MM.yyyy HH:mm:ss", System.currentTimeMillis()),
            "app_name" to "Трубочки-Будущее"
        )

        val payload = mapOf(
            "service_id" to SERVICE_ID,
            "template_id" to TEMPLATE_ID,
            "user_id" to PUBLIC_KEY,
            "template_params" to templateParams
        )

        val json = gson.toJson(payload)

        val request = Request.Builder()
            .url("https://api.emailjs.com/api/v1.0/email/send")
            .post(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json))
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    onError("Нет соединения с интернетом")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Handler(Looper.getMainLooper()).post {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onError("Ошибка отправки. Попробуйте позже")
                    }
                }
                response.close()
            }
        })
    }
}