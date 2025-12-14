# Keep application class and entry points
-keep public class com.example.wastetoworth.AaharApp { *; }
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# Keep all classes that could be dynamically loaded by the app
-keepclasseswithmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep all the entry point classes in the main dex file
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep all classes that are referenced by the manifest
-keep class * extends android.app.Activity
-keep class * extends android.app.Fragment
-keep class * extends android.support.v4.app.Fragment
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.app.backup.BackupAgentHelper
-keep class * extends android.preference.Preference
