import re

with open('Aurelian/app/src/main/java/com/aurelian/app/InviteCodeScreen.kt', 'r') as f:
    content = f.read()

# Fix the hardcoded string formatting string interpolation bug that happened during bash echo
content = content.replace('val response = NetworkClient.apiService.verifyInvite("Bearer $pendingToken", VerifyInviteRequest(inviteCode))', 'val response = NetworkClient.apiService.verifyInvite("Bearer " + pendingToken, VerifyInviteRequest(inviteCode))')

# Fix the TextField modifiers to be standard
search_text_field = r"""            OutlinedTextField\(value = inviteCode, onValueChange = \{ inviteCode = it\.uppercase\(\) \}, textStyle = LocalTextStyle\.current\.copy\(textAlign = TextAlign\.Center, fontSize = 24\.sp, letterSpacing = 8\.sp, fontWeight = FontWeight\.Bold, color = Gold\), placeholder = \{ Text\("______", modifier = Modifier\.fillMaxWidth\(\), textAlign = TextAlign\.Center, fontSize = 24\.sp, letterSpacing = 8\.sp, color = Silver\.copy\(alpha = 0\.2f\)\) \}, singleLine = true, keyboardOptions = KeyboardOptions\(capitalization = KeyboardCapitalization\.Characters\), colors = OutlinedTextFieldDefaults\.colors\(focusedBorderColor = Color\.Transparent, unfocusedBorderColor = Color\.Transparent, cursorColor = Gold\), modifier = Modifier\.fillMaxWidth\(\)\)"""
replace_text_field = """            OutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it.uppercase() },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, letterSpacing = 8.sp, fontWeight = FontWeight.Bold, color = Gold),
                placeholder = { Text("______", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 24.sp, letterSpacing = 8.sp, color = Silver.copy(alpha = 0.2f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Gold
                ),
                modifier = Modifier.fillMaxWidth()
            )"""
content = re.sub(search_text_field, replace_text_field, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/InviteCodeScreen.kt', 'w') as f:
    f.write(content)
