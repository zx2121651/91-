import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/Network.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update the SubmitAssetsRequest
old_req = "data class SubmitAssetsRequest(val documentUrls: List<String>)"
new_req = "data class SubmitAssetsRequest(val documentUrls: List<String>, val type: String = \"IDENTITY\", val notes: String = \"\")"

if old_req in content:
    content = content.replace(old_req, new_req)

# Add VettingStatusResponse and route
new_responses = """
data class VettingStatusItem(val type: String, val status: String, val createdAt: String, val rejectReason: String?)
data class VettingStatusResponse(val data: List<VettingStatusItem>)
"""

if "VettingStatusResponse" not in content:
    content = content.replace("data class SubmitAssetsResponse(val status: String)", new_responses + "data class SubmitAssetsResponse(val status: String)")


old_submit = """    @POST("api/v1/vetting/submit-assets")
    suspend fun submitAssets(@Body request: SubmitAssetsRequest): SubmitAssetsResponse"""

new_submit = """    @POST("api/v1/vetting/submit-assets")
    suspend fun submitAssets(@Body request: SubmitAssetsRequest): SubmitAssetsResponse

    @GET("api/v1/vetting/status")
    suspend fun getVettingStatus(): VettingStatusResponse"""

if "getVettingStatus" not in content:
    content = content.replace(old_submit, new_submit)

with open(file_path, "w") as f:
    f.write(content)
