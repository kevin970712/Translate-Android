package com.android.sttranslate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class STTranslationResponse(
    @SerialName("pronunciation")
    val pronunciation: String? = null,
    @SerialName("source_language")
    val sourceLanguage: String? = null,
    @SerialName("translated_text")
    val translatedText: String = ""
)