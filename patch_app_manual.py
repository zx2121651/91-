import re

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

# Just inject it directly if the class is simple enough.
content = content.replace('super.onCreate()', 'super.onCreate()\n        VideoCacheManager.initialize(this)')

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(content)
