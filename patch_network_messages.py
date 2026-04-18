import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/Network.kt"
with open(file_path, "r") as f:
    content = f.read()

old_message = "data class MessagesListResponse(val data: List<Message>)"
new_message = """data class Message(
    val msgId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val type: String,
    val timestamp: String,
    val status: String,
    val isMe: Boolean,
    val isEphemeral: Boolean = false,
    val ephemeralDurationSeconds: Int = 0,
    val readAt: String? = null,
    val expiresAt: String? = null
)
data class MessagesListResponse(val data: List<Message>)

data class ReadMessageResponseData(val readAt: String?, val expiresAt: String?)
data class ReadMessageResponse(val success: Boolean, val data: ReadMessageResponseData)"""

if "val readAt: String?" not in content:
    content = content.replace(old_message, new_message)

old_send = "data class SendMessageRequest(val convId: String, val content: String)"
new_send = "data class SendMessageRequest(val convId: String, val content: String, val type: String = \"TEXT\", val isEphemeral: Boolean = false, val ephemeralDurationSeconds: Int = 5)"

if "val isEphemeral: Boolean = false" not in content:
    content = content.replace(old_send, new_send)


old_api = """    @POST("api/v1/messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): SendMessageResponse"""

new_api = """    @POST("api/v1/messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): SendMessageResponse

    @POST("api/v1/messages/{msgId}/read")
    suspend fun markMessageAsRead(@Path("msgId") msgId: String): ReadMessageResponse"""

if "markMessageAsRead" not in content:
    content = content.replace(old_api, new_api)

with open(file_path, "w") as f:
    f.write(content)
