import urllib.request
import re
import os

font_dir = "app/src/main/res/font"
os.makedirs(font_dir, exist_ok=True)

def download_font(family, weights, file_prefix):
    url = f"https://fonts.googleapis.com/css?family={family}:{','.join(weights)}"
    
    # Old User-Agent forces TTF responses
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13'})
    
    with urllib.request.urlopen(req) as response:
        css = response.read().decode('utf-8')
    
    print(css[:500])
    
    weight_name_map = {
        '400': 'regular',
        '500': 'medium',
        '600': 'semibold',
        '700': 'bold'
    }
    
    for weight in weights:
        pattern = r'font-weight:\s*' + weight + r'.*?url\((https://[^)]+\.ttf)\)'
        match = re.search(pattern, css, re.DOTALL | re.IGNORECASE)
        
        if match:
            ttf_url = match.group(1)
            filename = f"{file_prefix}_{weight_name_map[weight]}.ttf"
            filepath = os.path.join(font_dir, filename)
            print(f"Downloading {filename} from {ttf_url}...")
            urllib.request.urlretrieve(ttf_url, filepath)
        else:
            print(f"Could not find TTF URL for {family} weight {weight}")

download_font("Quicksand", ["400", "500", "600", "700"], "quicksand")
download_font("Plus+Jakarta+Sans", ["400", "500", "600", "700"], "plusjakartasans")

try:
    if os.path.exists(os.path.join(font_dir, "quicksand.ttf")):
        os.remove(os.path.join(font_dir, "quicksand.ttf"))
    if os.path.exists(os.path.join(font_dir, "plusjakartasans.ttf")):
        os.remove(os.path.join(font_dir, "plusjakartasans.ttf"))
except Exception:
    pass
