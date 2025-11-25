# ProGuard rules for Glyph Matrix Toy Editor
#
# Copyright (c) 2024 Glyph Matrix Toy Editor Contributors
# Licensed under the MIT License

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep data classes for Gson serialization
-keep class com.glyphmatrix.toyeditor.data.models.** { *; }
-keep class com.glyphmatrix.toyeditor.gdk.GdkFrame { *; }
-keep class com.glyphmatrix.toyeditor.gdk.GdkAnimationPayload { *; }

# Keep GDK related classes
-keep class com.glyphmatrix.toyeditor.gdk.** { *; }

# Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Nothing SDK classes (if present)
-keep class com.nothing.ketchum.** { *; }
-dontwarn com.nothing.ketchum.**
