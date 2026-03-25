import re

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'r') as f:
    content = f.read()

search_pattern = r"""class AurelianApp : Application\(\) \{
    override fun onCreate\(\) \{
        super\.onCreate\(\)
    \}
\}"""

replace_pattern = """class AurelianApp : Application() {
    override fun onCreate() {
        super.onCreate()
        VideoCacheManager.initialize(this)
    }
}"""

new_content = re.sub(search_pattern, replace_pattern, content)

with open('Aurelian/app/src/main/java/com/aurelian/app/AurelianApp.kt', 'w') as f:
    f.write(new_content)
