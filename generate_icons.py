import os
from PIL import Image, ImageDraw

source_image_path = r"C:\Users\Ayush Shukla\.gemini\antigravity\brain\ea37a3cb-0c7e-4770-a2cb-66aa2bfe2227\.user_uploaded\media_1790626566760.jpg"
res_dir = r"C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\res"
store_icon_path = r"C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\store_icon.png"

# Densities for standard launcher icons (48dp) and adaptive foregrounds (108dp)
densities = {
    "mdpi": {"legacy": 48, "adaptive": 108},
    "hdpi": {"legacy": 72, "adaptive": 162},
    "xhdpi": {"legacy": 96, "adaptive": 216},
    "xxhdpi": {"legacy": 144, "adaptive": 324},
    "xxxhdpi": {"legacy": 192, "adaptive": 432},
}

def make_circle_icon(img, size):
    # Resize first
    img_resized = img.resize((size, size), Image.Resampling.LANCZOS)
    
    # Create circular mask
    mask = Image.new('L', (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size, size), fill=255)
    
    # Apply mask
    result = Image.new('RGBA', (size, size), (0,0,0,0))
    result.paste(img_resized, (0, 0), mask=mask)
    return result

def main():
    img = Image.open(source_image_path).convert("RGBA")
    
    # 1. Store icon
    img_1024 = img.resize((1024, 1024), Image.Resampling.LANCZOS)
    img_1024.save(store_icon_path, "PNG")
    print(f"Saved store icon: {store_icon_path}")
    
    # 2. Generate mipmap sizes
    for density, sizes in densities.items():
        mipmap_dir = os.path.join(res_dir, f"mipmap-{density}")
        os.makedirs(mipmap_dir, exist_ok=True)
        
        legacy_size = sizes["legacy"]
        adaptive_size = sizes["adaptive"]
        
        # ic_launcher.png (Legacy Square)
        img_legacy = img.resize((legacy_size, legacy_size), Image.Resampling.LANCZOS)
        img_legacy.save(os.path.join(mipmap_dir, "ic_launcher.png"), "PNG")
        
        # ic_launcher_round.png (Legacy Round)
        img_round = make_circle_icon(img, legacy_size)
        img_round.save(os.path.join(mipmap_dir, "ic_launcher_round.png"), "PNG")
        
        # ic_launcher_foreground.png (Adaptive Foreground)
        img_adaptive = img.resize((adaptive_size, adaptive_size), Image.Resampling.LANCZOS)
        img_adaptive.save(os.path.join(mipmap_dir, "ic_launcher_foreground.png"), "PNG")
        
        print(f"Generated {density} icons")

if __name__ == "__main__":
    main()
