# ProGuard & R8 Optimization Rules for JARVIS AI Boss Edition

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep Room Database and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Moshi models
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class * {
    @com.squareup.moshi.JsonClass(generateAdapter = true) *;
}

# Keep JARVIS Entities and Models
-keep class com.example.data.db.entity.** { *; }
-keep class com.example.data.model.** { *; }
