import re

file_path = "Aurelian/app/src/main/AndroidManifest.xml"
with open(file_path, "r") as f:
    content = f.read()

permissions = """
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-feature android:name="android.hardware.camera" android:required="false" />
"""

if "android.permission.CAMERA" not in content:
    content = content.replace('<uses-permission android:name="android.permission.INTERNET" />', permissions)

with open(file_path, "w") as f:
    f.write(content)
