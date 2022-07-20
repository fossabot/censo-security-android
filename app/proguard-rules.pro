# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class com.raygun.raygun4android.** { *; }
-keepattributes Exceptions, Signature, InnerClasses, SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# Keep the classes that are used for deserialization
-keep class com.strikeprotocols.mobile.data.** { *; }

-keep class com.strikeprotocols.mobile.presentation.durable_nonce.** { *; }