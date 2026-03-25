import re

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'r') as f:
    content = f.read()

# Remove the 'status' parameter since it doesn't exist in the User model
content = content.replace(',\n                        status = "Verified Collector"', '')
content = content.replace(',\n                        status = "Elite Member"', '')

with open('Aurelian/app/src/main/java/com/aurelian/app/MainFeedViewModel.kt', 'w') as f:
    f.write(content)
