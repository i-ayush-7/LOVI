import os

path = r'C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\java\com\example\focusbuilder\ui\HomeScreen.kt'
with open(path, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

content = content.replace('Text("+\'",', 'Text("->",')
content = content.replace('Text("??",', 'Text("P",') # Will fix
content = content.replace('Text("??",', 'Text("*",') # Will fix
content = content.replace('iconStr = "dY?"', 'iconStr = "H"')
content = content.replace('iconStr = "dYO?"', 'iconStr = "W"')
content = content.replace('iconStr = "dY -"', 'iconStr = "E"')
content = content.replace('Text("",', 'Text("U",') # Any other junk

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
