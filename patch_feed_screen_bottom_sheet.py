import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add necessary imports
missing_imports = """
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.lazy.LazyColumn
"""
if "import androidx.compose.material3.ModalBottomSheet" not in content:
    content = content.replace("import androidx.compose.material3.AlertDialog", missing_imports + "\nimport androidx.compose.material3.AlertDialog")

# Add sheet state at the top of MainFeedScreen
if "var showCommentsSheet by remember { mutableStateOf(false) }" not in content:
    content = content.replace(
        "var matchedUser by remember { mutableStateOf<User?>(null) }",
        "var matchedUser by remember { mutableStateOf<User?>(null) }\n    var showCommentsSheet by remember { mutableStateOf(false) }\n    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)"
    )

# Replace the "私信" logic in FeedItem to open the bottom sheet
# We need to change the FeedItem signature
content = re.sub(
    r'fun FeedItem\(user: User, isSelected: Boolean, onNavigateToProfile: \(String\) -> Unit, onNavigateToMasquerade: \(\) -> Unit, onLike: \(\) -> Unit\)',
    'fun FeedItem(user: User, isSelected: Boolean, onNavigateToProfile: (String) -> Unit, onNavigateToMasquerade: () -> Unit, onLike: () -> Unit, onShowComments: () -> Unit)',
    content
)

# And update the FeedItem invocation
content = content.replace(
    'FeedItem(user = users[page], isSelected = page == pagerState.currentPage, onNavigateToProfile = onNavigateToProfile, onNavigateToMasquerade = onNavigateToMasquerade, onLike = { viewModel.likeUser(users[page]) })',
    'FeedItem(user = users[page], isSelected = page == pagerState.currentPage, onNavigateToProfile = onNavigateToProfile, onNavigateToMasquerade = onNavigateToMasquerade, onLike = { viewModel.likeUser(users[page]) }, onShowComments = { showCommentsSheet = true })'
)

# Change the comment button in FeedItem
old_comment_btn = """                IconButton(
                    onClick = { Toast.makeText(context, "私密社交，禁止公开评论，请直接私信", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.MailOutline, contentDescription = "私信", tint = iconTint, modifier = Modifier.size(iconSize))
                }"""

new_comment_btn = """                IconButton(
                    onClick = onShowComments,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.MailOutline, contentDescription = "留言", tint = iconTint, modifier = Modifier.size(iconSize))
                }"""

content = content.replace(old_comment_btn, new_comment_btn)

# Add the BottomSheet UI in MainFeedScreen at the end of Scaffold / inside the main Box
sheet_code = """
                if (showCommentsSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showCommentsSheet = false },
                        sheetState = sheetState,
                        containerColor = DeepBlack,
                        scrimColor = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.7f) // 半屏
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "私密讨论区",
                                color = Gold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Mock 评论流
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(5) { index ->
                                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.DarkGray)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("匿名高定会员 ${index + 1}", color = Silver.copy(alpha=0.7f), fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("这里的氛围太棒了，简直是数字时代的凡尔赛宫。", color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }

                            // 输入框
                            var commentText by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    placeholder = { Text("在此留下您的品位...", color = Color.Gray, fontSize = 14.sp) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Gold,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(25.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(
                                    onClick = {
                                        if (commentText.isNotBlank()) {
                                            commentText = ""
                                            Toast.makeText(context, "留言已发送", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Gold, CircleShape)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "发送", tint = DeepBlack)
                                }
                            }
                        }
                    }
                }
"""

if "ModalBottomSheet" not in content:
    # insert before the last closing brace of MainFeedScreen
    # Let's use a regex to insert it safely before the final matchedUser Alert
    # or just right before the end of Box
    content = content.replace("                matchedUser?.let { user ->", sheet_code + "\n                matchedUser?.let { user ->")

with open(file_path, "w") as f:
    f.write(content)
