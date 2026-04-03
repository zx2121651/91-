with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

import re

# Fix AurelianApp
content = re.sub(r"LoginScreen\(onLoginSuccess = \{\n\s*navController\.navigate", "LoginScreen(onLoginSuccess = { token, status ->\n                    navController.navigate", content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)

# Fix AuthViewModel
with open('Aurelian/app/src/main/java/com/aurelian/app/AuthViewModel.kt', 'r') as f:
    auth_content = f.read()

auth_content = re.sub(r"NetworkClient\.apiService\.verifyInvite\(VerifyInviteRequest\(inviteCode\)\)", "NetworkClient.apiService.verifyInvite(\"Bearer token\", VerifyInviteRequest(inviteCode))", auth_content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AuthViewModel.kt', 'w') as f:
    f.write(auth_content)
