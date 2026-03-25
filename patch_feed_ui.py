import re

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'r') as f:
    content = f.read()

# Add missing import for TextOverflow
import_section = "import androidx.compose.ui.text.font.FontWeight"
new_import_section = "import androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.style.TextOverflow"
content = content.replace(import_section, new_import_section)

# Update the User Info Overlay
search_pattern = r"""        // Extremely clean User Info Overlay \(Bottom Left\)
        Column\(
            modifier = Modifier
                \.align\(Alignment\.BottomStart\)
                \.padding\(start = 16\.dp, end = 64\.dp, bottom = 90\.dp\) // Leave space for BottomNav and Right Actions
        \) \{
            Text\(
                text = user\.name,
                color = Silver,
                fontSize = 20\.sp,
                fontWeight = FontWeight\.SemiBold,
                letterSpacing = 0\.5\.sp
            \)
            Spacer\(modifier = Modifier\.height\(4\.dp\)\)
            Text\(
                text = user\.location\.uppercase\(\),
                color = Gold,
                fontSize = 11\.sp,
                fontWeight = FontWeight\.Medium,
                letterSpacing = 1\.sp
            \)
            Spacer\(modifier = Modifier\.height\(10\.dp\)\)
            Text\(
                text = user\.bio,
                color = Silver\.copy\(alpha = 0\.8f\),
                fontSize = 13\.sp,
                lineHeight = 18\.sp
            \)
        \}"""

replace_pattern = """        // Extremely clean User Info Overlay (Bottom Left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 90.dp) // Leave MORE space for Right Actions to avoid overlap
        ) {
            Text(
                text = user.name,
                color = Silver,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.location.uppercase(),
                color = Gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = user.bio,
                color = Silver.copy(alpha = 0.8f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2, // Restrict bio to 2 lines max
                overflow = TextOverflow.Ellipsis
            )
        }"""

new_content = re.sub(search_pattern, replace_pattern, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedScreen.kt', 'w') as f:
    f.write(new_content)
