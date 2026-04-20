import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/Network.kt"
with open(file_path, "r") as f:
    content = f.read()

# 添加新的 Data Class 和 API 方法
# 寻找 data class UploadUrlResponse 作为锚点
insert_data_class = """
data class PublishVideoRequest(val title: String, val bio: String, val mediaId: String)
data class PublishVideoResponse(val success: Boolean, val message: String)
"""

if "PublishVideoRequest" not in content:
    content = content.replace("data class ConfirmMediaResponse(val success: Boolean, val processing: Boolean)", "data class ConfirmMediaResponse(val success: Boolean, val processing: Boolean)\n" + insert_data_class)

insert_api = """
    // 10. Publish Video
    @POST("api/v1/feed/publish")
    suspend fun publishVideo(@Body request: PublishVideoRequest): PublishVideoResponse
"""

if "publishVideo(" not in content:
    content = content.replace("suspend fun getHookupRequestStatus(@Path(\"id\") id: String): HookupRequestResponse", "suspend fun getHookupRequestStatus(@Path(\"id\") id: String): HookupRequestResponse\n" + insert_api)

with open(file_path, "w") as f:
    f.write(content)
