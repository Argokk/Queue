package com.example.queues.api

import com.example.queues.auth.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {

    const val URL = "http://10.0.2.2:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Клиент БЕЗ токена — для login/register
    private val publicHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // Клиент С токеном — для защищённых запросов
    private val privateHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val token = TokenManager.getToken()

            val request = if (token.isNullOrBlank()) {
                originalRequest
            } else {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            }

            chain.proceed(request)
        }
        .addInterceptor(logging)
        .build()

    private val publicRetrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(publicHttpClient)
        .build()

    private val privateRetrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(privateHttpClient)
        .build()

    val usersApi: UsersApi = publicRetrofit.create(UsersApi::class.java)

    val enterpriseApi: EnterpriseApi = privateRetrofit.create(EnterpriseApi::class.java)

    val queueApi: QueueApi = privateRetrofit.create(QueueApi::class.java)

    val queueEntryApi: QueueEntryApi = privateRetrofit.create(QueueEntryApi::class.java)
}