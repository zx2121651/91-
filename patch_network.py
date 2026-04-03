import re

with open('Aurelian/app/src/main/java/com/aurelian/app/Network.kt', 'r') as f:
    content = f.read()

# Update the VerifyInviteData model
search_model = r"data class VerifyInviteData\(val valid: Boolean, val referrerId: String\)"
replace_model = "data class VerifyInviteData(val valid: Boolean, val referrerId: String, val newToken: String?)"
content = re.sub(search_model, replace_model, content)

# Update the interface signature
search_api = r"""    @POST\("api/v1/auth/verify-invite"\)
    suspend fun verifyInvite\(@Body request: VerifyInviteRequest\): VerifyInviteResponse"""
replace_api = """    @POST("api/v1/auth/verify-invite")
    suspend fun verifyInvite(@retrofit2.http.Header("Authorization") token: String, @Body request: VerifyInviteRequest): VerifyInviteResponse"""
content = re.sub(search_api, replace_api, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/Network.kt', 'w') as f:
    f.write(content)
