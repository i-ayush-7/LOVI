import cv2
import numpy as np
import urllib.request
import os

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

def process_image(url, out_path):
    print(f"Downloading {out_path}...")
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        img_array = np.asarray(bytearray(response.read()), dtype=np.uint8)
    
    img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)
    
    # The background is solid cream at the edges.
    # We will floodfill from (0,0) to find the exact background area.
    h, w = img.shape[:2]
    mask = np.zeros((h+2, w+2), np.uint8)
    
    # We use a strict floodfill to get the pure background
    cv2.floodFill(img.copy(), mask, (0,0), (0,0,0), (10,10,10), (10,10,10), cv2.FLOODFILL_MASK_ONLY)
    
    # mask is 1 for background. Trim the +2 borders.
    bg_mask = mask[1:h+1, 1:w+1]
    
    # Now we have a binary mask of the background.
    # To fix fringing, we want to expand the background mask slightly, but with anti-aliasing.
    # We can compute an alpha channel from the original image.
    # For any pixel, its distance to the background color (img[0,0])
    bg_color = img[0,0].astype(np.float32)
    diff = np.sqrt(np.sum(np.square(img.astype(np.float32) - bg_color), axis=2))
    
    # Let's say if a pixel is within the dilated background mask, we calculate its alpha based on distance
    kernel = np.ones((5,5), np.uint8)
    dilated_bg = cv2.dilate(bg_mask, kernel, iterations=1)
    
    # Initial alpha: 255 for foreground, 0 for background
    alpha = np.where(bg_mask == 1, 0, 255).astype(np.uint8)
    
    # For pixels in the transition zone (dilated_bg == 1 but bg_mask == 0), 
    # we soften the alpha based on color difference to eliminate fringing
    transition = (dilated_bg == 1) & (bg_mask == 0)
    
    # Map distance 0..30 to alpha 0..255
    soft_alpha = np.clip(diff[transition] * (255.0 / 30.0), 0, 255).astype(np.uint8)
    alpha[transition] = soft_alpha
    
    # Smooth the alpha channel slightly
    alpha = cv2.GaussianBlur(alpha, (3,3), 0)
    
    # Add alpha channel
    img_rgba = cv2.cvtColor(img, cv2.COLOR_BGR2BGRA)
    img_rgba[:,:,3] = alpha
    
    # Optional: for pixels with partial alpha, we can 'un-premultiply' or just leave them. 
    # To avoid white fringing, let's adjust the RGB values of partial alpha pixels 
    # to be closer to their inner neighbor? Actually, standard alpha blend is fine if we use 
    # a color distance alpha.
    
    # Resize to 512x512
    resized = cv2.resize(img_rgba, (512, 512), interpolation=cv2.INTER_AREA)
    
    cv2.imwrite(out_path, resized)
    print(f"Saved to {out_path}")

for name, url in urls.items():
    out_path = os.path.join(output_dir, f"{name}.png")
    process_image(url, out_path)
