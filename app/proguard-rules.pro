# Keep all classes under net.daum package
-keep class net.daum.** { *; }

# Keep all classes under android.opengl package
-keep class android.opengl.** { *; }

# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models.
# Modify this rule to fit the structure of your app.
-keepclassmembers class com.pjm.cours.data.** {
   *;
}

