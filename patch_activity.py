with open('Aurelian/app/src/main/java/com/aurelian/app/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('super.onCreate(savedInstanceState)', 'super.onCreate(savedInstanceState)\n        VideoCacheManager.initialize(applicationContext)')

with open('Aurelian/app/src/main/java/com/aurelian/app/MainActivity.kt', 'w') as f:
    f.write(content)
