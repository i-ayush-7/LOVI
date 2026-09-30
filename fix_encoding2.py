import os

path = r'C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\java\com\example\focusbuilder\ui\HomeScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# I know they are currently somewhat garbled. Let me just replace the exact lines that got garbled.
import re

# Replace Person IconButton text
content = re.sub(r'Text\(".*?", style = MaterialTheme.typography.titleMedium\)', 'Text("??", style = MaterialTheme.typography.titleMedium)', content)

# Replace Sun Text
content = re.sub(r'Text\(".*?", style = MaterialTheme.typography.labelMedium\)', 'Text("??", style = MaterialTheme.typography.labelMedium)', content)

# Replace Arrow Text
content = re.sub(r'Text\(".*?", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White\)', 'Text("?", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)', content)

# Replace BottomNavItem iconStr
content = re.sub(r'BottomNavItem\(iconStr = ".*?", label = "Today"', 'BottomNavItem(iconStr = "??", label = "Today"', content)
content = re.sub(r'BottomNavItem\(\s*iconStr = ".*?",\s*label = "World"', 'BottomNavItem(\n                iconStr = "??",\n                label = "World"', content)
content = re.sub(r'BottomNavItem\(\s*iconStr = ".*?",\s*label = "Explore"', 'BottomNavItem(\n                iconStr = "??",\n                label = "Explore"', content)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
