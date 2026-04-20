import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

bad_string = 'Text(\n                                "已选择: \\$startSec 秒 - \\$endSec 秒",\n                                color = Gold,\n                                fontSize = 14.sp\n                            )'
good_string = 'Text(\n                                "已选择: " + startSec + " 秒 - " + endSec + " 秒",\n                                color = Gold,\n                                fontSize = 14.sp\n                            )'

content = content.replace(bad_string, good_string)

with open(file_path, "w") as f:
    f.write(content)
