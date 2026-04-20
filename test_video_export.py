import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Verify that the correct parameters are passed
if "VideoEditorCore.processVideo" in content and "filterName = selectedFilter" in content and "watermarkText = title" in content:
    print("Export integration is properly configured.")
else:
    print("Missing export arguments in VideoEditScreen.kt")
