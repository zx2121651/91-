import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/VideoEditScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add BackHandler import
if "import androidx.activity.compose.BackHandler" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.activity.compose.BackHandler")

# Replace the beginning of VideoEditScreen to handle draft restoration
old_start = """fun VideoEditScreen(
    videoUri: String,
    onBack: () -> Unit,
    onNavigateToFeed: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("滤镜") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val filters = listOf("原画", "胶片(Film)", "黑白(B&W)", "电影感", "漏光(Leak)")
    var selectedFilter by remember { mutableStateOf(filters[0]) }

    val audioTracks = listOf("原声", "古典弦乐", "慵懒爵士", "深夜黑胶", "氛围电子")
    var selectedAudio by remember { mutableStateOf(audioTracks[0]) }

    // ExoPlayer 及视频时长状态
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var videoDurationMs by remember { mutableStateOf(15000L) } // 默认 15s
    var sliderRange by remember { mutableStateOf(0f..1f) }"""

new_start = """fun VideoEditScreen(
    videoUri: String,
    isDraft: Boolean = false,
    onBack: () -> Unit,
    onNavigateToFeed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("滤镜") }

    val filters = listOf("原画", "胶片(Film)", "黑白(B&W)", "电影感", "漏光(Leak)")
    var selectedFilter by remember { mutableStateOf(filters[0]) }

    val audioTracks = listOf("原声", "古典弦乐", "慵懒爵士", "深夜黑胶", "氛围电子")
    var selectedAudio by remember { mutableStateOf(audioTracks[0]) }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var videoDurationMs by remember { mutableStateOf(15000L) }
    var sliderRange by remember { mutableStateOf(0f..1f) }

    var showDraftDialog by remember { mutableStateOf(false) }

    // 初始化草稿恢复逻辑
    LaunchedEffect(isDraft) {
        if (isDraft) {
            DraftManager.getDraft(context)?.let { draft ->
                title = draft.title
                bio = draft.bio
                selectedFilter = draft.selectedFilter
                selectedAudio = draft.selectedAudio
                sliderRange = draft.sliderStart..draft.sliderEnd
            }
        }
    }

    // 拦截返回事件
    val handleBackPress = {
        // 如果内容有变动或者非空，则提示保存草稿
        if (title.isNotBlank() || selectedFilter != "原画" || selectedAudio != "原声" || sliderRange.start > 0f || sliderRange.endInclusive < 1f) {
            showDraftDialog = true
        } else {
            exoPlayer?.stop()
            onBack()
        }
    }

    BackHandler {
        handleBackPress()
    }

    // 退出提示保存草稿的弹窗
    if (showDraftDialog) {
        AlertDialog(
            onDismissRequest = { showDraftDialog = false },
            title = { Text("保存草稿", color = Gold, fontWeight = FontWeight.Bold) },
            text = { Text("您还有未完成的剪辑，是否保存到草稿箱以便下次继续？", color = Silver) },
            confirmButton = {
                TextButton(onClick = {
                    val draft = DraftData(
                        videoUri = videoUri,
                        title = title,
                        bio = bio,
                        selectedFilter = selectedFilter,
                        selectedAudio = selectedAudio,
                        sliderStart = sliderRange.start,
                        sliderEnd = sliderRange.endInclusive,
                        timestamp = System.currentTimeMillis()
                    )
                    DraftManager.saveDraft(context, draft)
                    showDraftDialog = false
                    exoPlayer?.stop()
                    onBack()
                }) {
                    Text("保存", color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    DraftManager.clearDraft(context)
                    showDraftDialog = false
                    exoPlayer?.stop()
                    onBack()
                }) {
                    Text("不保存", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1B1B1B)
        )
    }"""

content = content.replace(old_start, new_start)

# Update navigationIcon onClick in TopAppBar to use handleBackPress
content = content.replace("""                    IconButton(onClick = {
                        exoPlayer?.stop()
                        onBack()
                    })""", "                    IconButton(onClick = handleBackPress)")

# Update "完成并发布" to clear draft on success
content = content.replace("onNavigateToFeed()", "DraftManager.clearDraft(context)\n                                onNavigateToFeed()")

with open(file_path, "w") as f:
    f.write(content)
