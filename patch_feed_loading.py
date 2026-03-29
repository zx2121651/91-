import re

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'r') as f:
    content = f.read()

# Make sure imports are clean and correct
if "import androidx.compose.animation.core.LinearEasing" not in content:
    content = content.replace("import androidx.compose.animation.core.tween", "import androidx.compose.animation.core.tween\nimport androidx.compose.animation.core.LinearEasing\nimport androidx.compose.animation.core.infiniteRepeatable\nimport androidx.compose.animation.core.animateFloat\nimport androidx.compose.animation.core.rememberInfiniteTransition")

search_loading = r"""        is FeedUiState\.Loading -> \{
            Box\(modifier = Modifier\.fillMaxSize\(\)\.background\(DeepBlack\), contentAlignment = Alignment\.Center\) \{
                CircularProgressIndicator\(color = Gold\)
            \}
        \}"""

replace_loading = """        is FeedUiState.Loading -> {
            // High-end Skeleton Loader (Breathing Animation)
            val infiniteTransition = rememberInfiniteTransition(label = "breathing")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepBlack),
                contentAlignment = Alignment.Center
            ) {
                // Instead of a cheap spinner, we show a glowing luxury motif or placeholder
                Text(
                    text = "AURELIAN NIGHT",
                    color = Gold.copy(alpha = alpha),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                )
            }
        }"""

content = re.sub(search_loading, replace_loading, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'w') as f:
    f.write(content)
