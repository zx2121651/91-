import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace("VideoEditorCore.trimVideo", "VideoEditorCore.processVideo")
content = content.replace(
"""                                        VideoEditorCore.processVideo(
                                            context = context,
                                            inputUri = Uri.parse(videoUri),
                                            startMs = startMs,
                                            endMs = endMs,
                                            outputFile = outputFile
                                        )""",
"""                                        VideoEditorCore.processVideo(
                                            context = context,
                                            inputUri = Uri.parse(videoUri),
                                            startMs = startMs,
                                            endMs = endMs,
                                            filterName = selectedFilter,
                                            watermarkText = title,
                                            outputFile = outputFile
                                        )"""
)

with open(file_path, "w") as f:
    f.write(content)
