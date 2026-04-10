import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/HookupRequestsScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Fix some scope and compose errors in HookupRequestsScreen.kt
# Specifically the respondToRequest logic inside the Composable.
bad_logic = """    fun respondToRequest(id: String, action: String, userName: String) {
        coroutineScope.launch {
            try {
                val response = NetworkClient.apiService.respondHookupRequest(id, RespondHookupRequest(action))
                Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                if (action == "ACCEPT" && response.data?.conversationId != null) {
                    onNavigateToChat(userName)
                } else {
                    loadRequests("RECEIVED")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "处理异常", Toast.LENGTH_SHORT).show()
            }
        }
    }"""

good_logic = """    val respondToRequest = { id: String, action: String, userName: String ->
        coroutineScope.launch {
            try {
                val response = NetworkClient.apiService.respondHookupRequest(id, RespondHookupRequest(action))
                Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                if (action == "ACCEPT" && response.data?.conversationId != null) {
                    onNavigateToChat(userName)
                } else {
                    loadRequests("RECEIVED")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "处理异常", Toast.LENGTH_SHORT).show()
            }
        }
    }"""

content = content.replace(bad_logic, good_logic)

with open(file_path, "w") as f:
    f.write(content)
