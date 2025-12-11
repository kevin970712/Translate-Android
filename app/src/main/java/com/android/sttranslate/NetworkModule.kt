package com.android.sttranslate

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object NetworkModule {
    private const val BASE_URL = "https://simplytranslate.org/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val api: SimplyTranslateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SimplyTranslateApi::class.java)
    }
}
