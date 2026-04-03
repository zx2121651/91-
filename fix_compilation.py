import re

# 1. Fix AurelianApp.kt - remove duplicate composable("login") or adjust the existing one
with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

# The original file might have a login route already, let's just make sure we only have one
# and the parameter matches `onLoginSuccess: (String, String) -> Unit`
# For a quick fix, I will use sed/awk style targeted replacement or simple string replace if it's uniquely identifiable

search = r"""        composable\("login"\) \{
            LoginScreen\(onLoginSuccess = \{
                navController\.navigate\(Screen\.MainFeed\.route\) \{
                    popUpTo\("login"\) \{ inclusive = true \}
                \}
            \}\)
        \}"""

content = re.sub(search, "", content) # Remove old simple signature block

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)

# 2. Fix AuthViewModel.kt
with open('Aurelian/app/src/main/java/com/aurelian/app/AuthViewModel.kt', 'r') as f:
    auth_vm = f.read()

# Adjust the interface call to match our new (@Header token, @Body request) signature
search_vm = r"val response = NetworkClient\.apiService\.verifyInvite\(VerifyInviteRequest\(inviteCode\)\)"
replace_vm = """// Requires the token. Hardcoding a placeholder here or passing it as param
                val dummyPendingToken = "Bearer pending_token"
                val response = NetworkClient.apiService.verifyInvite(dummyPendingToken, VerifyInviteRequest(inviteCode))"""
auth_vm = re.sub(search_vm, replace_vm, auth_vm)

with open('Aurelian/app/src/main/java/com/aurelian/app/AuthViewModel.kt', 'w') as f:
    f.write(auth_vm)
