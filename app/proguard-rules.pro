-keepclassmembers class com.xtoolbox.ui.screen.module.WebUIActivity$KsuBridge {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.topjohnwu.superuser.** { *; }
-keep class net.lingala.zip4j.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
