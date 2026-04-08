import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add audio track state
if "var selectedAudio" not in content:
    content = content.replace(
        'val filters = listOf("原画", "胶片(Film)", "黑白(B&W)", "电影感", "漏光(Leak)")\n    var selectedFilter by remember { mutableStateOf(filters[0]) }',
        'val filters = listOf("原画", "胶片(Film)", "黑白(B&W)", "电影感", "漏光(Leak)")\n    var selectedFilter by remember { mutableStateOf(filters[0]) }\n\n    val audioTracks = listOf("原声", "古典弦乐", "慵懒爵士", "深夜黑胶", "氛围电子")\n    var selectedAudio by remember { mutableStateOf(audioTracks[0]) }'
    )

# Replace the placeholder text in the "配乐" tab with a LazyRow
placeholder = 'Text("高格调配乐库即将上线（古典 / 爵士 / 氛围电子）", color = Silver.copy(alpha = 0.5f), fontSize = 14.sp)'

new_audio_ui = """LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(audioTracks) { trackName ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedAudio == trackName) Color(0xFF333333) else Color(0xFF1E1E1E))
                                        .border(
                                            2.dp,
                                            if (selectedAudio == trackName) Gold else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedAudio = trackName },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(trackName, color = Silver, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }"""

if placeholder in content:
    content = content.replace(placeholder, new_audio_ui)

# Pass selectedAudio to processVideo
if "watermarkText = title," in content and "audioTrack = selectedAudio" not in content:
    content = content.replace(
        """                                            filterName = selectedFilter,
                                            watermarkText = title,
                                            outputFile = outputFile""",
        """                                            filterName = selectedFilter,
                                            watermarkText = title,
                                            audioTrack = selectedAudio,
                                            outputFile = outputFile"""
    )


with open(file_path, "w") as f:
    f.write(content)
