import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Fix the broken signature
bad_sig = "fun MainFeedScreen(onNavigateToEvents: () -> Unit = {}, onNavigateToProfile: (String) -> Unit = {}, onNavigateToMasquerade: () -> Unit = {}, onNavigateToPublish: () -> Unit = {}) -> Unit = {},\n    onNavigateToMasquerade: () -> Unit = {},\n    onNavigateToProfile: (String) -> Unit = {},\n    viewModel: MainFeedViewModel = viewModel()\n) {"

good_sig = """fun MainFeedScreen(
    onNavigateToEvents: () -> Unit = {},
    onNavigateToMasquerade: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToPublish: () -> Unit = {},
    viewModel: MainFeedViewModel = viewModel()
) {"""

content = content.replace(bad_sig, good_sig)

with open(file_path, "w") as f:
    f.write(content)
