# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Alberto\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Add this global rule
-keepattributes Signature

# ===========================================================
# Firebase Realtime Database - Data Models (CRITICAL FIX v2.0.4)
# ===========================================================
# Firebase uses reflection to serialize/deserialize objects
# We must prevent ProGuard from obfuscating class names, field names, and getters/setters

# Keep all data model classes and their structure
-keep class al.ahgitdevelopment.municion.datamodel.** {
    *;
}

# Keep getters and setters for Firebase reflection (CRITICAL)
-keepclassmembers class al.ahgitdevelopment.municion.datamodel.** {
    public <methods>;
    <fields>;
}

# Keep constructors (Firebase needs default constructor)
-keepclassmembers class al.ahgitdevelopment.municion.datamodel.** {
    public <init>();
    public <init>(...);
}

# ===========================================================
# Parcelable Implementation
# ===========================================================

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ===========================================================
# Firebase Core Rules
# ===========================================================

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep annotations for Firebase
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# For Firebase Crashlytics line numbers
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# ===========================================================
# Billing
# ===========================================================

-keep class com.android.billingclient.** { *; }

# ===========================================================
# Jetpack Compose Navigation
# ===========================================================
# Keep the names of navigation routes to prevent issues with string-based routing in release builds.
-keepnames class al.ahgitdevelopment.municion.ui.navigation.** { *; }

# ===========================================================
# Room entities (v3.3 sync redesign)
# ===========================================================
# These classes are @Parcelize + @Serializable. They are:
#   - written/read by Room (KSP-generated DAOs reflect on field names)
#   - serialized/deserialized by kotlinx.serialization in the outbox worker
#     (SyncOutboxWorker.processOperation uses Json.decodeFromString<Licencia>)
#   - serialized by NavType.serializeAsValue for type-safe navigation
# Without these rules the outbox payload decode would crash in release builds
# the moment any user edits an entity offline (the entity is in Room but
# can't be parsed back from the outbox JSON).
-keep class al.ahgitdevelopment.municion.data.local.room.entities.** { *; }

# Kotlinx Serialization needs the synthetic `$Companion` and `$serializer`
# of every @Serializable class. Standard rules from the official docs.
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

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ===========================================================
# Sync subsystem (v3.3 redesign)
# ===========================================================
# The outbox worker uses reflection-light kotlinx.serialization and
# the SyncOperation enum-as-string constants. Keep the whole subsystem.
-keep class al.ahgitdevelopment.municion.data.sync.** { *; }
-keep class al.ahgitdevelopment.municion.data.local.room.dao.** { *; }

# ===========================================================
# Hilt + WorkManager
# ===========================================================
# The HiltWorkerFactory uses reflection on the @HiltWorker-annotated
# constructor. Keep our workers.
-keep class al.ahgitdevelopment.municion.data.sync.SyncOutboxWorker { *; }
-keep class al.ahgitdevelopment.municion.data.sync.TombstoneCleanupWorker { *; }
