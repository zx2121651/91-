import re

file_path = "Aurelian/app/build.gradle"
with open(file_path, "r") as f:
    content = f.read()

transformer_deps = """
    implementation "androidx.media3:media3-transformer:1.2.1"
    implementation "androidx.media3:media3-effect:1.2.1"
"""

if "media3-transformer" not in content:
    content = content.replace('implementation "androidx.media3:media3-ui:1.2.1"', 'implementation "androidx.media3:media3-ui:1.2.1"\n' + transformer_deps)

with open(file_path, "w") as f:
    f.write(content)
