package com.vicgarci.repro

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

@Serializable
data class ResponseBody1(
    @SerialName("status") val status: String? = null,
)

@Serializable
data class ResponseBody2(
    @SerialName("status") val status: String? = null,
)

interface ExampleApiService {

    @GET("endpoint/v1")
    suspend fun getOperation1(): Response<ResponseBody1>

    @GET("endpoint/v2")
    suspend fun getOperation2(): Response<ResponseBody2>

    companion object {
        fun create(): ExampleApiService {
            return Retrofit.Builder()
                .baseUrl("https://www.google.com/")
                .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(ExampleApiService::class.java)
        }
    }
}