import cv2
import numpy as np
import urllib.request
import os

url = "https://lh3.googleusercontent.com/aida/AEtjO1UgyOzSLVu3if9XskoHZi3gIInfZ7aH_OF8fH-EfNm2hALq9S_mHHp2cY29edKn0YI7rhZ92tb-BGyHD7gJydk_0y8qNaSiDGBVI22feou7DCXX5QdfqaE5ZGUKT3Fud4PpJu3s1rXvGv_38plP8a6MiOYDVz332cwRUwIz4NJwqy8qWNkSdOuEhrSGgRudDA4q1Klk-vne6sUBnLbsbuJsHNYqWFJE-czXNdEdjCs38khqJAm3pUPO"
out_path = r"C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\res\drawable\illust_deer.png"

print(f"Downloading {out_path}...")
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req) as response:
    img_array = np.asarray(bytearray(response.read()), dtype=np.uint8)

img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

h, w = img.shape[:2]
mask = np.zeros((h+2, w+2), np.uint8)

# strict floodfill from (0,0) to get the pure cream background
cv2.floodFill(img.copy(), mask, (0,0), (0,0,0), (10,10,10), (10,10,10), cv2.FLOODFILL_MASK_ONLY)

bg_mask = mask[1:h+1, 1:w+1]

bg_color = img[0,0].astype(np.float32)
diff = np.sqrt(np.sum(np.square(img.astype(np.float32) - bg_color), axis=2))

kernel = np.ones((5,5), np.uint8)
dilated_bg = cv2.dilate(bg_mask, kernel, iterations=1)

alpha = np.where(bg_mask == 1, 0, 255).astype(np.uint8)
transition = (dilated_bg == 1) & (bg_mask == 0)

# Map distance 0..30 to alpha 0..255 for anti-aliasing
soft_alpha = np.clip(diff[transition] * (255.0 / 30.0), 0, 255).astype(np.uint8)
alpha[transition] = soft_alpha

alpha = cv2.GaussianBlur(alpha, (3,3), 0)

img_rgba = cv2.cvtColor(img, cv2.COLOR_BGR2BGRA)
img_rgba[:,:,3] = alpha

# Resize to 512x512
resized = cv2.resize(img_rgba, (512, 512), interpolation=cv2.INTER_AREA)

cv2.imwrite(out_path, resized)
print(f"Saved to {out_path}")

# Test composite
bg = np.zeros_like(resized)
bg[:] = (255, 0, 0, 255) # Blue background
test_alpha = resized[:, :, 3] / 255.0
for c in range(3):
    bg[:, :, c] = (test_alpha * resized[:, :, c] + (1 - test_alpha) * bg[:, :, c])

out_path_test = r"C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\res\drawable\test_deer_bg.png"
cv2.imwrite(out_path_test, bg)
print("Saved composite image for verification.")
