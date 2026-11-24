# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line number information for Play Console R8 deobfuscation
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses
-renamesourcefileattribute SourceFile

# CameraX reflection-loaded config provider (referenced by AndroidManifest meta-data)
-keep class androidx.camera.camera2.Camera2Config$DefaultProvider { *; }
-keep class androidx.camera.core.impl.** { *; }

# ZXing offline QR encoder/decoder
-keep class com.google.zxing.** { *; }

# PROSHIFT Data Models & Serialization
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }

# Moshi JSON Converter rules (if used for sync payload)
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <init>(...);
}
