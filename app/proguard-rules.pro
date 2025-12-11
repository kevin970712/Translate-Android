# ============================================
# 一般 Android 與 Kotlin 設定
# ============================================
-dontwarn javax.annotation.**
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# ============================================
# Kotlinx Serialization (JSON 解析)
# ============================================
-keep @kotlinx.serialization.Serializable class * {
    *;
}

# 保持自動生成的 serializer 類別
-keep class *$$serializer {
    *;
}

# 保持 Companion object
-keepclassmembers class * {
    static ** Companion;
}

-dontwarn kotlinx.serialization.**

# ============================================
# Retrofit (網路請求)
# ============================================
-keep interface com.android.sttranslate.SimplyTranslateApi { *; }

-keepattributes Signature
-keepattributes Exceptions

-dontwarn retrofit2.**
-dontnote retrofit2.Platform
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================
# Jetpack Compose
# ============================================
-keep class androidx.compose.** { *; }

# ============================================
# 專案特定規則 (Model 類別)
# ============================================
-keep class com.android.sttranslate.** { *; }

-keep,allowobfuscation,allowshrinking interface retrofit2.Call
 -keep,allowobfuscation,allowshrinking class retrofit2.Response
 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation