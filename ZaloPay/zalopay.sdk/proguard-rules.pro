# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/chucvv/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Remove debug, verbose, and info Log calls
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    ## Uncomment to remove warnings and errors as well
    public static *** w(...);
    public static *** e(...);
}

# --- RECOMMENDED ANDROID CONFIG ------------------------------------------

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}
-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}
-keepattributes LocalVariableTable,LocalVariableTypeTable
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Deprecated
-keepattributes SourceFile
-keepattributes LineNumberTable
-keepattributes *Annotation*
-keepattributes EnclosingMethod

#google analytic
-keep class com.google.android.apps.analytics.**{ *; }

#dialog
-keep class vn.com.zalopay.wallet.view.dialog.DialogManager

# support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

#retrofix 2.

-dontwarn retrofit2.Platform$Java8

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}

#okhttp3
-dontwarn com.google.gson.**

-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.*

# okio
-keep public class org.codehaus.**
-keep public class java.nio.**

-dontwarn java.nio.file.*
-dontwarn okio.**
-keep class sun.misc.Unsafe { *; }
-keep class okio.** { *; }


# cardview
-keep class io.codetail.widget.RevealLinearLayout
#protobuf
#-dontwarn com.google.protobuf.**
#-keep class com.google.protobuf.** { *; }

#netty
#-dontwarn io.netty.**
#-keep class io.netty.** {
#    *;
#}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

#self
-dontwarn vn.com.zalopay.wallet.**
-keep interface vn.com.zalopay.wallet.** { *; }
-keep class vn.com.zalopay.wallet.** { *; }

-dontwarn java.lang.invoke.**
-keep class java.lang.invoke.** { *; }

# If your application, applet, servlet, library, etc., contains enumeration
# classes, you'll have to preserve some special methods. Enumerations were
# introduced in Java 5. The java compiler translates enumerations into classes
# with a special structure. Notably, the classes contain implementations of some
# static methods that the run-time environment accesses by introspection (Isn't
# that just grand? Introspection is the self-modifying code of a new
# generation). You have to specify these explicitly, to make sure they aren't
# removed or obfuscated:

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# More complex applications, applets, servlets, libraries, etc., may contain
# classes that are serialized. Depending on the way in which they are used, they
# may require special attention

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#timber
-dontwarn org.jetbrains.annotations.**


