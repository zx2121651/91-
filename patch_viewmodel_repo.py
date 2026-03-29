import re

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'r') as f:
    content = f.read()

# Make sure we import Flow collection extensions if not present
if "import kotlinx.coroutines.flow.collectLatest" not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.flow.collectLatest")

# Instantiate the repo
search_class_def = r"class MainFeedViewModel : ViewModel\(\) \{"
replace_class_def = """class MainFeedViewModel : ViewModel() {

    // Simple manual injection of our new Repository
    private val repository = FeedRepository()"""
content = re.sub(search_class_def, replace_class_def, content)

# Rewrite fetchVideos
search_fetch = r"""    fun fetchVideos\(\) \{
        viewModelScope\.launch \{
            _uiState\.value = FeedUiState\.Loading
            try \{
                // Injecting real H\.264/HEVC mock video streams for testing cache and rendering
                val mockData = listOf\(
                    User\(
                        id = "mock_user_1",
                        name = "Alexandre R\.",
                        location = "Monaco Yacht Club",
                        bio = "Enjoying the summer breeze\. #Monaco",
                        videoUrl = "http://commondatastorage\.googleapis\.com/gtv-videos-bucket/sample/ForBiggerBlazes\.mp4"
                    \),
                    User\(
                        id = "mock_user_2",
                        name = "Eleanor V\.",
                        location = "Paris, France",
                        bio = "Night stroll around the Louvre\.",
                        videoUrl = "http://commondatastorage\.googleapis\.com/gtv-videos-bucket/sample/ForBiggerEscapes\.mp4"
                    \),
                    User\(
                        id = "mock_user_3",
                        name = "Sebastian K\.",
                        location = "Geneva, Switzerland",
                        bio = "Testing the limits of time\.",
                        videoUrl = "http://commondatastorage\.googleapis\.com/gtv-videos-bucket/sample/ForBiggerJoyrides\.mp4"
                    \)
                \)

                // Pre-cache the first video immediately
                preloadVideo\(mockData\.firstOrNull\(\)\?\.videoUrl\)
                _uiState\.value = FeedUiState\.Success\(mockData\)
                // val response = NetworkClient\.apiService\.getFeedVideos\(\)
                // _uiState\.value = FeedUiState\.Success\(response\.data\)
            \} catch \(e: Exception\) \{
                Log\.e\("MainFeedViewModel", "Error fetching videos", e\)
                _uiState\.value = FeedUiState\.Error\(e\.localizedMessage \?: "网络错误或服务器未启动"\)
            \}
        \}
    \}"""

replace_fetch = """    fun fetchVideos() {
        viewModelScope.launch {
            // Only show Loading UI if we truly have nothing currently showing
            if (_uiState.value !is FeedUiState.Success) {
                _uiState.value = FeedUiState.Loading
            }

            repository.getFeedVideos().collectLatest { result ->
                result.onSuccess { users ->
                    // Always update UI with newest available data (from cache OR network)
                    _uiState.value = FeedUiState.Success(users)

                    // Immediately pre-cache the first video to ensure no black screen
                    // when the user scrolls to the first item
                    preloadVideo(users.firstOrNull()?.videoUrl)
                }.onFailure { exception ->
                    Log.e("MainFeedViewModel", "Failed to fetch videos from repository", exception)
                    // We only emit Error state if we didn't manage to load from Cache first
                    if (_uiState.value !is FeedUiState.Success) {
                        _uiState.value = FeedUiState.Error(exception.localizedMessage ?: "网络错误或服务器未响应")
                    }
                }
            }
        }
    }"""
content = re.sub(search_fetch, replace_fetch, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'w') as f:
    f.write(content)
