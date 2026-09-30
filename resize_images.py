from PIL import Image
import os

drawable_dir = "app/src/main/res/drawable"
images = ["illust_welcome.png", "illust_game_intro.png", "illust_celebration.png", "illust_dashboard.png"]

for img_name in images:
    path = os.path.join(drawable_dir, img_name)
    if os.path.exists(path):
        with Image.open(path) as img:
            img = img.resize((512, 512), Image.Resampling.LANCZOS)
            img.save(path, optimize=True, quality=85)
            print(f"Resized and optimized {img_name}")
