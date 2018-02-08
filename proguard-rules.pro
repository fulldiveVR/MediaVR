# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keep class com.google.** { *; }
-keep class org.videolan.** { *; }
-dontwarn com.google.**
-dontwarn org.videolan.**
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.picasso.**
-dontwarn java.lang.invoke**

-keepclasseswithmembernames class * {
  native <methods>;
}

-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,RuntimeVisibleAnnotations,AnnotationDefault,JavascriptInterface

-keep public class * {
    public protected *;
}

-keep class java.lang.invoke.** { *; }

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

# Needed by google-api-client to keep generic types and @Key annotations accessed via reflection
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keepclasseswithmembers class * {
  @com.google.api.client.util.Value <fields>;
}

-keepnames class com.google.api.client.http.HttpTransport
-keepnames class com.google.api.client.http.javanet.NetHttpTransport

# Needed by Guava (google-api-client)
-dontnote sun.misc.Unsafe
-dontwarn sun.misc.Unsafe


-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.javascript.**
-keep class in.fulldive.coub.** { *; }

-keepclassmembers class ** {
    public void onEvent*(**);
}

-keepclassmembers class in.fulldive.** {
    !private <fields>;
    protected <fields>;
    public <fields>;
    <methods>;
}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# EventBus 3.0
-keep class de.greenrobot.event.** { *; }
-keep class * {
    @de.greenrobot.event.* <methods>;
}


# Don't warn for missing support classes
-dontwarn de.greenrobot.event.util.*$Support
-dontwarn de.greenrobot.event.util.*$SupportManagerFragment

-keep class com.google.android.exoplayer.** {*;}
-keep class org.chromium.** {*;}

#Keep web libs untouched
-keep class com.fasterxml.** { *; }
-keep class org.w3c.** { *; }

-dontwarn org.w3c.**
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry


# Don't obfuscate any NDK/SDK code. This makes the debugging of stack traces in
# in release builds easier.
-keepnames class com.google.vr.ndk.** { *; }
-keepnames class com.google.vr.sdk.** { *; }

# These are part of the SDK <-> VrCore interfaces for GVR.
-keepnames class com.google.vr.vrcore.library.api.** { *; }

# These are part of the Java <-> native interfaces for GVR.
-keep class com.google.vr.** { native <methods>; }

-keep class com.google.vr.cardboard.annotations.UsedByNative
-keep @com.google.vr.cardboard.annotations.UsedByNative class *
-keepclassmembers class * {
    @com.google.vr.cardboard.annotations.UsedByNative *;
}

-keep class com.google.vr.cardboard.UsedByNative
-keep @com.google.vr.cardboard.UsedByNative class *
-keepclassmembers class * {
    @com.google.vr.cardboard.UsedByNative *;
}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
# For using GSON @Expose annotation
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class in.fulldive.*.model.** { *; }
# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
##---------------End: proguard configuration for Gson  ----------

-keep class com.android.vending.billing.**


# Webview
-keep public class com.fulldive.browser.widget$JSInterface
-keep public class * implements com.fulldive.browser.widget$JSInterface
-keepclassmembers class com.fulldive.browser.widget$JSInterface {
    <methods>;
}