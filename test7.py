import re
with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'r') as f:
    content = f.read()

# Add repository variable
if "private val repository = FeedRepository()" not in content:
    content = content.replace("class MainFeedViewModel : ViewModel() {", "class MainFeedViewModel : ViewModel() {\n\n    private val repository = FeedRepository()")

# Add flow imports if missing
if "import kotlinx.coroutines.flow.collectLatest" not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.flow.collectLatest")

# Overwrite fetchVideos simply by searching for the function signature to the end of its block
search_regex = re.compile(r"    fun fetchVideos\(\) \{.*?\}", re.DOTALL)
replace_str = """    fun fetchVideos() {
        viewModelScope.launch {
            if (_uiState.value !is FeedUiState.Success) {
                _uiState.value = FeedUiState.Loading
            }

            repository.getFeedVideos().collectLatest { result ->
                result.onSuccess { users ->
                    _uiState.value = FeedUiState.Success(users)
                    preloadVideo(users.firstOrNull()?.videoUrl)
                }.onFailure { exception ->
                    Log.e("MainFeedViewModel", "Failed to fetch videos from repository", exception)
                    if (_uiState.value !is FeedUiState.Success) {
                        _uiState.value = FeedUiState.Error(exception.localizedMessage ?: "网络错误或服务器未响应")
                    }
                }
            }
        }
    }"""

# Manual replacement based on known code block to avoid regex misses
content_lines = content.split('\n')
new_lines = []
skip = False
for line in content_lines:
    if line.startswith("    fun fetchVideos() {"):
        skip = True
        new_lines.append(replace_str)
        continue
    if skip and line.startswith("    private val _matchEvent"):
        skip = False
    if not skip:
        new_lines.append(line)

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'w') as f:
    f.write('\n'.join(new_lines))
