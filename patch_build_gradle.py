import re

file_path = "Aurelian/app/build.gradle"
with open(file_path, "r") as f:
    content = f.read()

camerax_dependencies = """
    // CameraX core library using the camera2 implementation
    def camerax_version = "1.3.1"
    implementation "androidx.camera:camera-core:${camerax_version}"
    implementation "androidx.camera:camera-camera2:${camerax_version}"
    // If you want to additionally use the CameraX Lifecycle library
    implementation "androidx.camera:camera-lifecycle:${camerax_version}"
    // If you want to additionally use the CameraX VideoCapture library
    implementation "androidx.camera:camera-video:${camerax_version}"
    // If you want to additionally use the CameraX View class
    implementation "androidx.camera:camera-view:${camerax_version}"
"""

if "androidx.camera:camera-core" not in content:
    content = content.replace("dependencies {", "dependencies {\n" + camerax_dependencies, 1)

with open(file_path, "w") as f:
    f.write(content)
