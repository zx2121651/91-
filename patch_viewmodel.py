import re

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'r') as f:
    content = f.read()

search_pattern = r"""    fun fetchVideos\(\) \{
        viewModelScope\.launch \{
            _uiState\.value = FeedUiState\.Loading
            try \{
                val response = NetworkClient\.apiService\.getFeedVideos\(\)
                _uiState\.value = FeedUiState\.Success\(response\.data\)
            \} catch \(e: Exception\) \{
                Log\.e\("MainFeedViewModel", "Error fetching videos", e\)
                _uiState\.value = FeedUiState\.Error\(e\.localizedMessage \?: "网络错误或服务器未启动"\)
            \}
        \}
    \}"""

replace_pattern = """    fun fetchVideos() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            try {
                // Forcing the use of extreme mock data for UI boundary testing
                val mockData = listOf(
                    User(
                        id = "mock_user_1",
                        name = "Alexandre de Rothschild",
                        location = "Monaco Yacht Club, Principality of Monaco",
                        bio = "这是一个非常极端的长文本简介测试。在 Aurelian Night，我们追求的是极致的私密与奢华。即便是长到像这样足足有四五行的文字，我们也必须保证它能被优雅地折叠，绝对不能像野草一样遮挡住右侧那排精致的半透明金色操作图标。保持高定感。",
                        videoUrl = "https://test-videos.co.uk/vids/jellyfish/mp4/1080/Jellyfish_1080_10s_1MB.mp4",
                        status = "Verified Collector"
                    ),
                    User(
                        id = "mock_user_2",
                        name = "Eleanor V.",
                        location = "Paris, France",
                        bio = "Short elegant bio.",
                        videoUrl = "https://test-videos.co.uk/vids/jellyfish/mp4/1080/Jellyfish_1080_10s_1MB.mp4",
                        status = "Elite Member"
                    )
                )
                _uiState.value = FeedUiState.Success(mockData)
                // val response = NetworkClient.apiService.getFeedVideos()
                // _uiState.value = FeedUiState.Success(response.data)
            } catch (e: Exception) {
                Log.e("MainFeedViewModel", "Error fetching videos", e)
                _uiState.value = FeedUiState.Error(e.localizedMessage ?: "网络错误或服务器未启动")
            }
        }
    }"""

new_content = re.sub(search_pattern, replace_pattern, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'w') as f:
    f.write(new_content)
