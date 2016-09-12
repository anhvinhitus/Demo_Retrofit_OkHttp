# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/AnhHieu/Library/Android/sdk/tools/proguard/proguard-android.txt
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

#-ignorewarnings # ***************************** Comment lại để biết chi tiết các warning *****************************

-keepclassmembers class ** {
    public void onEvent*(***);
}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}


# Disabling obfuscation is useful if you collect stack traces from production crashes
# (unless you are using a system that supports de-obfuscate the stack traces).
# -dontobfuscate

-keep class vn.com.vng.zalopay.react.** { *; }
-keep class vn.com.vng.zalopay.domain.** { *; }
# -keep class vn.com.vng.zalopay.data.** { *; }
-keep class vn.com.vng.zalopay.event.** { *; }
-keep class vn.com.vng.zalopay.data.api.response.** { *; }
-keepattributes Signature, *Annotation*

# React Native

# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.proguard.annotations.DoNotStrip
-keep,allowobfuscation @interface com.facebook.proguard.annotations.KeepGettersAndSetters
-keep class com.facebook.react.bridge.Promise
-keep class com.facebook.react.bridge.ReadableMap
-keep class com.facebook.react.bridge.ReadableArray
-keep class com.facebook.react.bridge.queue.NativeRunnable { *; }

-dontwarn bolts.**

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.proguard.annotations.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.proguard.annotations.DoNotStrip *;
}

-keepclassmembers @com.facebook.proguard.annotations.KeepGettersAndSetters class * {
  void set*(***);
  *** get*();
}

-keep class * extends com.facebook.react.bridge.JavaScriptModule { *; }
-keep class * extends com.facebook.react.bridge.NativeModule { *; }
-keepclassmembers,includedescriptorclasses class * { native <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.UIProp <fields>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactProp <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactPropGroup <methods>; }

-dontwarn com.facebook.react.**


#retrolambda

-dontwarn java.lang.invoke.*

#Green Dao

-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

# okhttp

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# okhttp 3

#-dontwarn com.squareup.okhttp.**
#-dontwarn com.squareup.okhttp3.**
#-keep class com.squareup.okhttp3.** { *;}
#-dontwarn okio.*

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# okio

-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

# butterknife
# Retain generated class which implement ViewBinder.
#-keep public class * implements butterknife.internal.ViewBinder { public <init>(); }

# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinder.
#-keep class butterknife.*
#-keepclasseswithmembernames class * { @butterknife.* <methods>; }
#-keepclasseswithmembernames class * { @butterknife.* <fields>; }

#retrofit 2

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions

# Parcel library
-keep class **$$Parcelable { *; }

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
#-keep class com.google.gson.examples.android.model.** { *; }


#Event bus

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#glide

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule

#Netty

-keepattributes Signature,InnerClasses

-keep class org.apache.log4j.* {*;}

-keep class org.slf4j.Logger {*;}
-keep class org.slf4j.LoggerFactory {*;}
-keep class  org.slf4j.helpers.NOPLoggerFactory {*;}
-keep class org.slf4j.LoggerFactory {*;}

#
#-keep class java.util.concurrent.atomic.AtomicReferenceFieldUpdater {*;}
#-keep class java.util.concurrent.atomic.AtomicReferenceFieldUpdaterImpl{*;}


#leakcanary

-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }

#rxjava & rxandroid

-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#play-service

-keep public class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

##************OTHER**********

-keep public class com.sun.msv.** { *; }
-dontwarn com.sun.msv.**

-keep public class java.beans.** { *; }
-dontwarn java.beans.**

-keep public class org.relaxng.datatype.** { *; }
-dontwarn org.relaxng.datatype.**

-keep public class javax.xml.stream.** { *; }
-dontwarn javax.xml.stream.**

-keep public class org.gjt.xpp.** { *; }
-dontwarn org.gjt.xpp.**

-keep public class javax.xml.bind.** { *; }
-dontwarn javax.xml.bind.**

-keep public class org.jdom.** { *; }
-dontwarn org.jdom.**

-keep public class nu.xom.** { *; }
-dontwarn nu.xom.**

-keep public class javax.swing.tree.** { *; }
-dontwarn javax.swing.tree.**

-keep public class javax.swing.table.** { *; }
-dontwarn javax.swing.table.**

-keep public class org.dom4j.swing.** { *; }
-dontwarn org.dom4j.swing.**

-keep class com.android.vending.billing.**
-dontwarn com.android.vending.billing.**

-keep public class org.w3c.dom.** { *; }
-dontwarn org.w3c.dom.**

-keep public class org.ietf.jgss.** { *; }
-dontwarn org.ietf.jgss.**

-keep public class org.apache.http.** { *; }
-dontwarn org.apache.http.**


# https://github.com/facebook/fresco/issues/209
-keep class com.facebook.imagepipeline.gif.** { *; }
-keep class com.facebook.imagepipeline.webp.** { *; }

# Works around a bug in the animated GIF module which will be fixed in 0.12.0
-keep class com.facebook.imagepipeline.animated.factory.AnimatedFactoryImpl {
    public AnimatedFactoryImpl(com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory,com.facebook.imagepipeline.core.ExecutorSupplier);
}


#-dontwarn android.support.v7.**
#-keep class android.support.v7.** { *; }
#-keep interface android.support.v7.** { *; }

#-keep class android.support.v7.widget.RoundRectDrawable { *; }

#-dontwarn android.support.design.**
#-keep class android.support.design.** { *; }
#-keep interface android.support.design.** { *; }
#-keep public class android.support.design.R$* { *; }


-keep public class * extends com.google.protobuf.GeneratedMessage { *; }
#Warning:vn.com.zalopay.wallet.entity.protobuf.LogicMessages: can't find referenced class com.google.protobuf.Descriptors$Descriptor
-keepclassmembernames class * extends com.google.protobuf.GeneratedMessage { *; }
-keepclassmembers class vn.com.vng.zalopay.data.ws.protobuf.* {
    <methods>;
}

#Warning:vn.com.zalopay.wallet.entity.protobuf.LogicMessages$Login: can't find referenced class com.google.protobuf.UnknownFieldSet

#-keep public class com.google.protobuf.** { *; }

#-keep interface com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

-keep public interface com.sun.msv.datatype.SerializationContext
-keep public interface org.relaxng.datatype.** { *; }

-keep public interface javax.swing.tree.** { *; }
-keep public interface javax.swing.table.** { *; }
-keep public interface org.gjt.xpp.** { *; }


-keepattributes InnerClasses

-keep class **.R
-keep class **.R$* {
    <fields>;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-adaptresourcefilenames

-adaptclassstrings
-adaptresourcefilecontents **.xml

-dontskipnonpubliclibraryclassmembers

-dontwarn android.support.**

-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


#Payment SDK

-keep public class vn.com.zalopay.wallet.** {
  public protected *;
}

#-keep public class io.card.payment.** {
#  public protected *;
#}

-dontwarn vn.com.zalopay.wallet.**


# ---- REQUIRED card.io CONFIG ----------------------------------------
# card.io is a native lib, so anything crossing JNI must not be changed

# Don't obfuscate DetectionInfo or public fields, since
# it is used by native methods
#-keep class io.card.payment.DetectionInfo
#-keepclassmembers class io.card.payment.DetectionInfo {
#public *;
#}

#-keep class io.card.payment.CreditCard
#-keep class io.card.payment.CreditCard$1
#-keepclassmembers class io.card.payment.CreditCard {
#*;
#}

#-keepclassmembers class io.card.payment.CardScanner {
#*** onEdgeUpdate(...);
#}

# Don't mess with classes with native methods

-keepclasseswithmembers class * {
native <methods>;
}

-keepclasseswithmembernames class * {
native <methods>;
}

#-keep public class io.card.payment.* {
#public protected *;
#}

# required to suppress errors when building on android 22
#-dontwarn io.card.payment.CardIOActivity

#-keep class io.card.payment.OverlayView
#-keep class io.card.payment.Util

#Zalo SDK

-keep public class com.zing.zalo.zalosdk.** {
    public protected *;
}
-dontwarn com.zing.zalo.zalosdk.**

-keepclassmembers class com.zing.zalo.zalosdk.resource.R {
    *;
}



