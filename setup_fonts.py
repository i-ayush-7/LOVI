import urllib.request
import os

font_dir = "app/src/main/res/font"
os.makedirs(font_dir, exist_ok=True)

base_url = "https://raw.githubusercontent.com/google/fonts/main/ofl"

fonts = {
    "quicksand_regular.ttf": f"{base_url}/quicksand/static/Quicksand-Regular.ttf",
    "quicksand_medium.ttf": f"{base_url}/quicksand/static/Quicksand-Medium.ttf",
    "quicksand_semibold.ttf": f"{base_url}/quicksand/static/Quicksand-SemiBold.ttf",
    "quicksand_bold.ttf": f"{base_url}/quicksand/static/Quicksand-Bold.ttf",
    "plusjakartasans_regular.ttf": f"{base_url}/plusjakartasans/static/PlusJakartaSans-Regular.ttf",
    "plusjakartasans_medium.ttf": f"{base_url}/plusjakartasans/static/PlusJakartaSans-Medium.ttf",
    "plusjakartasans_semibold.ttf": f"{base_url}/plusjakartasans/static/PlusJakartaSans-SemiBold.ttf",
    "plusjakartasans_bold.ttf": f"{base_url}/plusjakartasans/static/PlusJakartaSans-Bold.ttf",
}

for filename, url in fonts.items():
    filepath = os.path.join(font_dir, filename)
    print(f"Downloading {filename}...")
    try:
        urllib.request.urlretrieve(url, filepath)
    except Exception as e:
        print(f"Failed to download {filename}: {e}")

print("Done downloading fonts.")

# Also let's check contrast
def hex_to_rgb(hex_str):
    hex_str = hex_str.lstrip('#')
    return tuple(int(hex_str[i:i+2], 16)/255.0 for i in (0, 2, 4))

def srgb_to_linear(c):
    return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4

def relative_luminance(rgb):
    r, g, b = [srgb_to_linear(x) for x in rgb]
    return 0.2126 * r + 0.7152 * g + 0.0722 * b

def contrast_ratio(hex1, hex2):
    l1 = relative_luminance(hex_to_rgb(hex1))
    l2 = relative_luminance(hex_to_rgb(hex2))
    light = max(l1, l2)
    dark = min(l1, l2)
    return (light + 0.05) / (dark + 0.05)

text_color = "#234A42"
sky_blue = "#B8E1F2"
lavender = "#D9D2F8"

print(f"Contrast Text on Sky Blue: {contrast_ratio(text_color, sky_blue):.2f}")
print(f"Contrast Text on Lavender: {contrast_ratio(text_color, lavender):.2f}")
