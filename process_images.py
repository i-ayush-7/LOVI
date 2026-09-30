import urllib.request
import os
from PIL import Image
import io

try:
    from rembg import remove
    USE_REMBG = True
except ImportError:
    print("rembg not found, using Pillow fallback")
    USE_REMBG = False

urls = {
    "illust_fox": "https://lh3.googleusercontent.com/aida/AEtjO1WI339dmLAMAn-R6k2j6XaFIeqbK4rFP8JXRPIGo0EmuPyaxrDlN_eBYJzMHhFDl4DPhfxwcHNBZEf2ubKMqePFPFl631hPUXd05_XvdDFI4xgUOXSehCtY63Uq1KH_iKro9KAHjFL5n7ji3OuWuqbDLcRWMtcmNQNv1zI8ZBCn1daO_eMXi62vFR2WHdzy5iD9DWNCtUoa0NNaQdfpW9pAx0Ka8BcVaTjHM0fWSyxt_YiDPobjlB1PnA",
    "illust_rabbit": "https://lh3.googleusercontent.com/aida/AEtjO1UxlEX57eFJyxMy_tZ1EAle__CKpAeH06xprtk7mPfxH3Nir59Ff9X7nxT3C-CXSUa0HiGF3cJjraYBvcCKzYVbHednFKCLeF9BlJig0igYLIQwccfkHBJT0XayfsJn0F_bwVpgtFyMKTz2pZUB6KRuDVyBpqW-3wRmKlE8rSTcRQbbY62KOWuqNbAsbTQYQYLZHobaF_l2Q1Xq157WSuK7kckDFs4M-EG1U-5N67hT-vJDLU4iLAU38Q",
    "illust_owl": "https://lh3.googleusercontent.com/aida/AEtjO1WtvDJYHUehM867d7cl6KEikXyVIhIG5ryD8cve64taxJ7fECMwXIz5LUzSIgoepAyldfyDWFFKcOCnN1EHGrLmMim81EgXLKq-3LrAk9A2E82FL9n9V-8_-WQRF9AUKugio-rI_2G5EmHsAgPhJpzLYNYVyBa7r6yaQp1XkcoFoykWj6M9SjwOsVHnO8ePH71CAFdxZQ50HhHqSSW2zrJnoCdCX_DRWHLcmAcNkY7d2VbiSmcc-svnIg",
    "illust_bear": "https://lh3.googleusercontent.com/aida/AEtjO1ULOQHaKUXXD-PcDVEHgOgpEIjEgpkB33LOtAuo7UpOhhb8SIprgwQ2bUUK2fI82mplYP2iLD-eL3GRp0MPl8ycCMQT_HXYmzZdCl1qNuqTmlIHamiHzOVk83BAcfdAZi2nL2_G3MUlOm7nQRiLGegmC3VmOvBeElZhXFR6WI2WVzE0F-EsxTeML9ZkDy3b2JwiwszSdESMu-hamBS1GxhCHNeFNmz_DBbkAexA9-PKJJmic48CSyhufA",
    "illust_deer": "https://lh3.googleusercontent.com/aida/AEtjO1XfIB0bB-I_Kq_URKFTNPp8NCC46JY3E7-PPvo0l1tKGf7dWSHMv2f9nYbdPYav9J-P0z8eJDvRSyVDo2zilh_HeRA_Fsw_NwSIANq1jm-8qHY_AU6JsxWtmazErlyiD0OaPoMjk-ZPiiQbJXHtlp0JN3yz8mc7GAIYDS0QBhr_ngbWTupu5HtL16oag56m1KgxkoiWBZ8tiHd4rPOLBqqO9GEX568Ef2ww2gS1pDTHjPyzK-wWdaKnAA",
    "illust_frog": "https://lh3.googleusercontent.com/aida/AEtjO1U97fWGycdKCNviH1_bQJGgrQvjE0eiD6HqBkhttsUbaRqa3P5jOtrCXpATxS02N6imZg2uj4Jt9IUNyYfMds7JLQmgKB5h-LLUIQP4KE_ovQDuKVNOTURCt18F7KeBufgyIlkCDd3PutriLU5pDL3eeA9NUOfq8iVjrUQK1oKsGxGFWyjuAzeHoTGjmBl420edBEQ7kGQJjrQNSZQ8bbiDZ36C7T2Whq5wHPMleooHDYaE92pKXN7j"
}

output_dir = r"C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\res\drawable"
os.makedirs(output_dir, exist_ok=True)

def remove_bg_pillow(img):
    img = img.convert("RGBA")
    data = img.getdata()
    new_data = []
    
    # We know the bg is cream #fdf9f1 or similar. Let's sample top-left pixel as BG
    bg_color = data[0]
    
    def color_distance(c1, c2):
        return sum(abs(a - b) for a, b in zip(c1[:3], c2[:3]))
    
    for item in data:
        # Give a small tolerance (15) to remove anti-aliasing slightly, but not perfect
        if color_distance(item, bg_color) < 25:
            new_data.append((255, 255, 255, 0))
        else:
            new_data.append(item)
    
    img.putdata(new_data)
    return img

for name, url in urls.items():
    print(f"Processing {name}...")
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        img_data = response.read()
    
    img = Image.open(io.BytesIO(img_data))
    
    # Remove background
    if USE_REMBG:
        img = remove(img)
    else:
        img = remove_bg_pillow(img)
    
    # Resize
    img = img.resize((512, 512), Image.Resampling.LANCZOS)
    
    out_path = os.path.join(output_dir, f"{name}.png")
    img.save(out_path, format="PNG", optimize=True)
    print(f"Saved to {out_path}")

print("Done processing images.")
