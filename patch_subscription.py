import re

file_path = "Aurelian/app/src/main/java/com/aurelian/app/SubscriptionScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add onNavigateToVetting parameter
old_sig = """fun SubscriptionScreen(onBack: () -> Unit) {"""
new_sig = """fun SubscriptionScreen(onBack: () -> Unit, onNavigateToVetting: (String) -> Unit = {}) {"""

if "onNavigateToVetting" not in content:
    content = content.replace(old_sig, new_sig)

# Update Button "Apply for Black Card" to actually navigate
old_btn = """                Button(
                    onClick = { /* TODO: Application Flow */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        "APPLY FOR BLACK CARD",
                        color = DeepBlack,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }"""

new_btn = """                Button(
                    onClick = { onNavigateToVetting("ASSETS") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        "提交资产审核以申请黑卡",
                        color = DeepBlack,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }"""

if "提交资产审核" not in content:
    content = content.replace(old_btn, new_btn)

with open(file_path, "w") as f:
    f.write(content)
