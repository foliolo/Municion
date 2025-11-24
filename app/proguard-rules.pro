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

# ============================================================
# Firebase Realtime Database - Data Models (CRITICAL FIX v2.0.4)
# ============================================================
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

# ============================================================
# Parcelable Implementation
# ============================================================

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ============================================================
# Firebase Core Rules
# ============================================================

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

# ============================================================
# Billing
# ============================================================

-keep class com.android.vending.billing.**
-keepclassmembers class al.ahgitdevelopment.municion.BillingUtil.** { public *; }
-keep public class al.ahgitdevelopment.municion.BillingUtil.**
