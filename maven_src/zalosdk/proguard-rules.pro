##-injars ./out.jar
##-outjars 'C:\Users\CPU11153-local\Desktop\New folder\Source\trunk\ZaloSDK\ZaloSDK\libs\ZaloPaymentSdk_2_0_1020_2.jar'
#
##-libraryjars ../../Dependency/dep/google-play-services.jar
##-libraryjars ../../Dependency/dep/android.jar
##-libraryjars ./libs/IInAppBillingService.jar
##-libraryjars ./libs/dom4j-1.6.1.jar
##-libraryjars ./libs/jaxen-1.1.6.jar
#-libraryjars ./libs/za-mobile-v1.1.0419.jar
#-libraryjars ./libs/zdk-core-v1.1.0328.jar
##-libraryjars ./libs/android-support-v4.jar
#
#-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
#-optimizationpasses 5
#-dontusemixedcaseclassnames
#-keepattributes *Annotation*,InnerClasses,Deprecated,Exceptions,JavascriptInterface,SuppressWarnings,Signature
#-dontpreverify
#-verbose
#-dontwarn android.support.**,org.**,com.google.android.gms.**
#
#-keep class com.android.vending.billing.**
#
## Preserve all fundamental application classes.
#-keep public class * extends android.app.Activity
#
#-keep public class * extends android.app.Application
#
#-keep public class * extends android.app.Service
#
#-keep public class * extends android.content.BroadcastReceiver
#
#-keep public class * extends android.content.ContentProvider
#
#-keep public class * extends android.app.Dialog
#
#
#-keep class com.zing.zalo.zalosdk.googlebilling.**
#-keep class com.zing.zalo.zalosdk.oauth.ZingMeBaseLoginView {
#	*;
#}
#
#-keep interface com.zing.zalo.zalosdk.payment.direct.ZaloPaymentListener {
#	*;
#}
#-keep interface com.zing.zalo.zalosdk.payment.direct.ZaloTransactionListener {
#	*;
#}
#
#-keep class com.zing.zalo.zalosdk.common.Utils {
#	*;
#}
#
#-keepclasseswithmembers public class com.zing.zalo.zalosdk.** {
#	*;
#}
#
#-keep class com.vng.zing.zdice.** {
#    *;
#}
#
#-keep class javax.** {
#    <fields>;
#    <methods>;
#}
#
#-keep class org.** {
#    <fields>;
#    <methods>;
#}
#
#-keep class com.google.android.gms.** {
#    <fields>;
#    <methods>;
#}
#
#-keepclassmembers class ** {
#    public void onEvent*(**);
#}
#
##-keepclassmembers class * {
##    @android.webkit.JavascriptInterface
##    <methods>;
##}
##
##-keepclassmembers class * {
##    @java.lang.SuppressWarnings
##    <methods>;
##}
#
## keep setters in Views so that animations can still work.
## see http://proguard.sourceforge.net/manual/examples.html#beans
#-keepclassmembers public class * extends android.view.View {
#    void set*(***);
#    *** get*();
#}
#
## We want to keep methods in Activity that could be used in the XML attribute onClick
#-keepclassmembers class * extends android.app.Activity {
#    public void *(android.view.View);
#}
#
#-keep class * extends android.os.Parcelable {
#    public static final android.os.Parcelable$Creator *;
#}
#
## For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
#-keepclasseswithmembers,allowshrinking class * {
#    native <methods>;
#}
#
## Also keep - Enumerations. Keep the special static methods that are required in
## enumeration classes.
#-keepclassmembers enum  * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}
#
#-keep public class com.zing.zalo.zalosdk.** { *; }
#-dontwarn com.zing.zalo.zalosdk.**
#
#-keep public class com.zing.zalo.zalosdk.resource.SyncR { *; }
#
#-keep public class com.zing.zalo.zalosdk.resource.R { *; }
#
#-keepclasseswithmembers public class com.zing.zalo.zalosdk.R {
#	*;
#}
#
#-keep public class com.zing.zalo.zalosdk.R {
#	*;
#}
#
#
#
#
##-keep public class com.zing.zalo.zalosdk.resource.** { *; }
##-dontwarn com.zing.zalo.zalosdk.resource.**
#
##Warning:com.zing.zalo.zalosdk.oauth.GcmMessageHandler: can't find referenced method 'java.lang.String getPackageName()' in program class com.zing.zalo.zalosdk.oauth.GcmMessageHandler
























 # To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Save the obfuscation mapping to a file, so you can de-obfuscate any stack
# traces later on.

#-injars 'C:\Users\CPU11153-local\workspace\ZaloSDK\libs\ZaloPaymentSdk_2_0_1214.jar'
#-outjars 'C:\Users\CPU11153-local\Desktop\out.jar'

#-injars ./libs/zdk-core-v1.1.0328.jar

#-libraryjars ./libs/IInAppBillingService.jar
#-libraryjars ./libs/dom4j-1.6.1.jar
#-libraryjars ./libs/jaxen-1.1.6.jar
#-libraryjars ./libs/za-mobile-v1.1.0419.jar
#-libraryjars ./libs/zdk-core-v1.1.0328.jar

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose


# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
#-dontoptimize
-dontpreverify

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5

# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-keepattributes *Annotation*,InnerClasses,Deprecated,Exceptions,JavascriptInterface,SuppressWarnings
-keep public class com.google.vending.licensing.ILicensingService

-keep public class javax.xml.stream.** { *; }
-dontwarn javax.xml.stream.**

#Warning:org.dom4j.datatype.SchemaParser: can't find referenced class org.relaxng.datatype.DatatypeException
#Warning:org.dom4j.datatype.SchemaParser: can't find referenced class com.sun.msv.datatype.xsd.XSDatatype

-keep public class org.relaxng.datatype.** { *; }
-dontwarn org.relaxng.datatype.**

-keep public class com.sun.msv.datatype.xsd.** { *; }
-dontwarn com.sun.msv.datatype.xsd.**

-keep public class java.beans.** { *; }
-dontwarn java.beans.**

-keep public class org.gjt.xpp.** { *; }
-dontwarn org.gjt.xpp.**

-keep public class javax.xml.bind.** { *; }
-dontwarn javax.xml.bind.**

-keep public class com.google.android.gms.auth.** { *; }
-dontwarn com.google.android.gms.auth.**

#-keep public class com.zing.zalo.zalosdk.oauth.** { *; }
#-dontwarn com.zing.zalo.zalosdk.oauth.**

-keep public class com.sun.msv.datatype.** { *; }
-dontwarn com.sun.msv.datatype.**

-keep public class javax.swing.tree.** { *; }
-dontwarn javax.swing.tree.**

-keep public class org.jdom.** { *; }
-dontwarn org.jdom.**

-keep public class nu.xom.** { *; }
-dontwarn nu.xom.**

-keep public class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.common.**

-keep public class org.dom4j.swing.** { *; }
-dontwarn org.dom4j.swing.**

-keep public class javax.swing.table.** { *; }
-dontwarn javax.swing.table.**

-keep public class  org.w3c.dom.** { *; }
-dontwarn  org.w3c.dom.**

-keep public class com.zing.zalo.zalosdk.** {
    public protected *;
}
-dontwarn com.zing.zalo.zalosdk.**

-keepattributes InnerClasses

-keep class **.R
-keep class **.R$* {
    <fields>;
}

-adaptresourcefilenames

-adaptclassstrings
-adaptresourcefilecontents **.xml


# Also keep - Swing UI L&F. Keep all extensions of javax.swing.plaf.ComponentUI,
# along with the special 'createUI' method.
-keep class * extends javax.swing.plaf.ComponentUI {
    public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent);
}

-keep public class com.zing.zalo.zalosdk.oauth.** {
    public protected *;
}
-dontwarn com.zing.zalo.zalosdk.oauth.**

-keep public class com.zing.zalo.zalosdk.payment.** {
    public protected *;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep public class com.zing.zalo.zalosdk.resource.R {
    public protected *;
}

-keep public class com.zing.zalo.zalosdk.resource.R$id {
    public protected *;
}

-keep public class com.zing.zalo.zalosdk.resource.R$drawable {
    public protected *;
}

-dontwarn com.zing.zalo.zalosdk.resource.**


-keepclassmembers class com.zing.zalo.zalosdk.resource.R {
    *;
}

-keepclassmembers class **.R$* {
     public static <fields>;
}

-dontobfuscate