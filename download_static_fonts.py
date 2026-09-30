import urllib.request
import re
import os

font_dir = "app/src/main/res/font"
os.makedirs(font_dir, exist_ok=True)

def download_font(family, weights, file_prefix):
    # CSS API URL
    url = f"https://fonts.googleapis.com/css2?family={family}:wght@{';'.join(weights)}"
    
    # Old User-Agent forces TTF responses instead of WOFF2
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0'})
    
    with urllib.request.urlopen(req) as response:
        css = response.read().decode('utf-8')
    
    for weight in weights:
        # Find the src url for this specific weight
        # Format in CSS: 
        # font-weight: 400;
        # src: url(https://...) format('truetype');
        
        # Regex to find the block for the specific weight and extract the URL
        pattern = r'font-weight:\s*' + weight + r'.*?url\((https://[^)]+\.ttf)\)'
        match = re.search(pattern, css, re.DOTALL | re.IGNORECASE)
        
        if match:
            ttf_url = match.group(1)
            
            weight_name_map = {
                '400': 'regular',
                '500': 'medium',
                '600': 'semibold',
                '700': 'bold'
            }
            
            filename = f"{file_prefix}_{weight_name_map[weight]}.ttf"
            filepath = os.path.join(font_dir, filename)
            
            print(f"Downloading {filename} from {ttf_url}...")
            urllib.request.urlretrieve(ttf_url, filepath)
        else:
            print(f"Could not find TTF URL for {family} weight {weight}")

download_font("Quicksand", ["400", "500", "600", "700"], "quicksand")
download_font("Plus+Jakarta+Sans", ["400", "500", "600", "700"], "plusjakartasans")

# Delete old variable fonts
try:
    os.remove(os.path.join(font_dir, "quicksand.ttf"))
    os.remove(os.path.join(font_dir, "plusjakartasans.ttf"))
    print("Deleted variable fonts.")
except Exception as e:
    print("Error deleting variable fonts:", e)
