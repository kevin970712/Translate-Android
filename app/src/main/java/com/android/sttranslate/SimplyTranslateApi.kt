package com.android.sttranslate

import retrofit2.http.GET
import retrofit2.http.Query

interface SimplyTranslateApi {
    @GET("api/translate/")
    suspend fun translate(
        @Query("engine") engine: String? = "google",
        @Query("from") source: String,
        @Query("to") target: String,
        @Query("text") query: String
    ): STTranslationResponse
}