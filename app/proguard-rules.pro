-dontwarn java.lang.reflect.AnnotatedType

-ignorewarnings

-optimizationpasses 7
-dontusemixedcaseclassnames

-verbose
-printmapping proguardMapping.txt

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-overloadaggressively
-allowaccessmodification

-adaptclassstrings
-adaptresourcefilenames
-adaptresourcefilecontents

-renamesourcefileattribute SourceFile

-keepattributes SourceFile,LineNumberTable

-keepattributes *Annotation*
-keepattributes Signature

-keep class com.simple.spiderman.** { *; }
-keepnames class com.simple.spiderman.** { *; }
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep public class * extends androidx.annotation.** { *; }
-keep public class * extends androidx.core.content.FileProvider

-keep class com.fosstool.app.hook.scope.CorePatch.** { *; }
-keepclassmembers class com.fosstool.app.hook.scope.CorePatch.** { *; }

-keep class com.fosstool.app.hook.MainHook { *; }
-keep class com.fosstool.app.hook.hooker.** { *; }
-keep class com.fosstool.app.hook.scope.** { *; }
-keep class com.fosstool.app.hook.statusbar.** { *; }
-keep class com.fosstool.app.hook.utils.** { *; }

-keep class org.luckypray.dexkit.** { *; }
-keep class de.robv.android.xposed.** { *; }
-dontwarn de.robv.android.xposed.**
