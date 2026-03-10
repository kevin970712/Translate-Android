-keep @kotlinx.serialization.Serializable class com.android.sttranslate.** { *; }
-keep class *$$serializer { *; }

-keep interface com.android.sttranslate.SimplyTranslateApi { *; }

-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation