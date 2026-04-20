import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Make sure the UI strings are in Chinese and well commented
content = content.replace('Text(text = "AURELIAN NIGHT"', 'Text(text = "AURELIAN NIGHT" /* 高端加载占位符 */')
content = content.replace('contentDescription = "Cover Image"', 'contentDescription = "视频封面" /* 视频加载前的封面图片占位 */')

with open(file_path, "w") as f:
    f.write(content)
