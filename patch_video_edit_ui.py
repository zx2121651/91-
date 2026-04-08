import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Change the "仅用于提示滤镜效果" text overlay, since it is now real.
old_hint = """                // 仅用于提示滤镜效果，后续需用 Media3 Effect 真正实现实时预览
                if (selectedFilter != "原画") {
                    Text(
                        "预览滤镜: \\$selectedFilter",
                        color = Gold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(DeepBlack.copy(alpha=0.6f), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    )
                }"""

new_hint = """                // 右上角状态标签
                if (selectedFilter != "原画") {
                    Text(
                        "已应用: \$selectedFilter",
                        color = Gold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(DeepBlack.copy(alpha=0.8f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }"""

content = content.replace(old_hint, new_hint)


# Change UI labels to be more professional
content = content.replace('Text("为这段瞬间命名..."', 'Text("添加专属文字水印..."')

with open(file_path, "w") as f:
    f.write(content)
