# R8 / ProGuard rules for the :androidApp module.
# NOTE: minify/shrink are currently disabled (see androidApp/build.gradle.kts). These
# rules are the baseline to re-enable during release hardening (KMP migration phase 10/11).

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
# Crashlytics readable stack traces
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# ===========================================================
# Firebase / Google Play Services (reflection-based)
# ===========================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ===========================================================
# Room entities (now in the :shared module). Used by Room (KSP DAOs reflect on
# field names), by kotlinx.serialization (outbox payloads) and by Realtime Database.
# ===========================================================
-keep class al.ahgitdevelopment.municion.data.local.room.entities.** { *; }

# ===========================================================
# Type-safe navigation routes (string-based routing in release)
# ===========================================================
-keepnames class al.ahgitdevelopment.municion.ui.navigation.** { *; }

# ===========================================================
# kotlinx.serialization (official rules)
# ===========================================================
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
