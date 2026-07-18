# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep stack traces readable in crash reports.
-keepattributes SourceFile,LineNumberTable

# PhotoSlideshowService is instantiated by the system Dream/Daydream framework from the manifest
# component name, outside our own call graph, so R8 can't trace that reference — keep it intact
# rather than risk a silent ClassNotFoundException when the screen saver starts.
-keep class com.wwwescape.photoslideshow.dream.** { *; }

# Strip verbose/debug/info logging from release builds (smaller dex, no diagnostic strings —
# e.g. content URIs — written to logcat). Warnings and errors are kept.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
