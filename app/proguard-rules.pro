# kotlinx.serialization : conserver les sérialiseurs générés des classes du projet.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.lionne.tv.**$$serializer { *; }
-keepclassmembers class com.lionne.tv.** {
    *** Companion;
}
-keepclasseswithmembers class com.lionne.tv.** {
    kotlinx.serialization.KSerializer serializer(...);
}
