import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/Network.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add HookupRequests endpoints to Network.kt
new_data = """
data class HookupRequestItem(val requestId: String, val userId: String, val name: String, val avatarUrl: String, val bio: String, val location: String, val isVerified: Boolean, val meetingType: String, val note: String, val safeMode: Boolean, val status: String, val createdAt: Long, val expiresAt: Long)
data class HookupRequestsResponse(val data: List<HookupRequestItem>)
data class RespondHookupData(val conversationId: String?)
data class RespondHookupResponse(val success: Boolean, val message: String, val data: RespondHookupData?)
data class RespondHookupRequest(val action: String)
"""

if "HookupRequestItem" not in content:
    content = content.replace("data class HookupRequestData(", new_data + "data class HookupRequestData(")

new_api = """    @GET("api/v1/hookups/requests")
    suspend fun getHookupRequests(@Query("type") type: String = "RECEIVED", @Query("limit") limit: Int = 20): HookupRequestsResponse
    @POST("api/v1/hookups/requests/{id}/respond")
    suspend fun respondHookupRequest(@Path("id") id: String, @Body request: RespondHookupRequest): RespondHookupResponse
"""

if "getHookupRequests" not in content:
    content = content.replace("suspend fun getHookupRequestStatus(@Path(\"id\") id: String): HookupRequestResponse", "suspend fun getHookupRequestStatus(@Path(\"id\") id: String): HookupRequestResponse\n" + new_api)

with open(file_path, "w") as f:
    f.write(content)
