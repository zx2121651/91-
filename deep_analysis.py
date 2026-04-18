import os
import re

def analyze_directory(directory):
    todos = []
    mocks = []
    empty_clicks = []

    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".kt"):
                path = os.path.join(root, file)
                with open(path, "r", encoding="utf-8") as f:
                    lines = f.readlines()
                    for i, line in enumerate(lines):
                        if "TODO" in line:
                            todos.append((file, i+1, line.strip()))
                        if "Mock" in line or "mock" in line:
                            mocks.append((file, i+1, line.strip()))
                        if re.search(r'onClick\s*=\s*\{\s*\}', line):
                            empty_clicks.append((file, i+1, line.strip()))

    return todos, mocks, empty_clicks

app_dir = "Aurelian/app/src/main/java/com/aurelian/app/"
t, m, e = analyze_directory(app_dir)

print(f"Found {len(t)} TODOs, {len(m)} Mocks, {len(e)} empty clicks.")
# Print some samples
print("\n--- TODOs ---")
for t_item in t[:5]: print(t_item)
print("\n--- Mocks ---")
for m_item in m[:5]: print(m_item)
print("\n--- Empty Clicks ---")
for e_item in e[:5]: print(e_item)
