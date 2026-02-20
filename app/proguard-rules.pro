# Documentation: https://developer.android.com/tools/help/proguard.html

# To see the full configuration used by ProGuard:
# -printconfiguration tmp/r8-config.txt

# To see which classes have been removed by ProGuard:
# -printusage tmp/r8-usage.txt

# To tell R8 to consider MyClass an entry point:
# -keep public class MyClass

# To be able to deobfuscate stack traces:
# The mapping is at app/build/outputs/mapping/release/mapping.txt
# You may upload it to Google Play Console to directly get deobfuscated stack traces.
-keepattributes LineNumberTable,SourceFile
