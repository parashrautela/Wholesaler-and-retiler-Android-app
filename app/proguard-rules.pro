# Jewel India Proguard Rules

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class * {
    companion object;
}
-keepclassmembers class * {
    public static final ** Companion;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# OkHttp & Ktor
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
