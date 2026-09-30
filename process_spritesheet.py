import cv2
import numpy as np
import urllib.request
import os

url = "https://lh3.googleusercontent.com/aida/AEtjO1UKlmBGKipept1m8Mh__o07yKRdrZLtfCfwVq5ldv68_0pBFnheuBAEXAy_JDljzhydIKT-WS_SwqJYRLuLDS9PPAtd188mGmpg2SaPisCAoqxqHL0bBocSHzk2RDPv1HzniasaFVpQ3RhazIGilrCSoIalU2euywAtCxK7-dCPSOJmaZ5k2VjMCU2tNjZPiiHadv8NyHkYqb5OThdcyl_yzQt9a3cqhyyKrC2g2xlOcqE0zDYoyM5qiw"

print("Downloading sprite sheet...")
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req) as response:
    img_array = np.asarray(bytearray(response.read()), dtype=np.uint8)

img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

# Find background color at (0,0)
bg_color = img[0,0].copy()

# Create a mask of the background
diff = np.max(np.abs(img.astype(np.int32) - bg_color.astype(np.int32)), axis=2)
_, binary = cv2.threshold(diff.astype(np.uint8), 20, 255, cv2.THRESH_BINARY)

# Find contours of the objects
contours, _ = cv2.findContours(binary, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
print(f"Found {len(contours)} contours")

# Filter small contours
bboxes = []
for c in contours:
    x, y, w, h = cv2.boundingRect(c)
    if w > 50 and h > 50:
        bboxes.append((x, y, w, h))

print(f"Found {len(bboxes)} valid bounding boxes")

# Sort bounding boxes top-to-bottom, left-to-right
# Group by rows using Y coordinate thresholding
bboxes.sort(key=lambda b: b[1]) # Sort by Y
rows = []
current_row = []
for b in bboxes:
    if not current_row:
        current_row.append(b)
    else:
        # If Y difference is small, it's the same row
        if abs(b[1] - current_row[0][1]) < 100:
            current_row.append(b)
        else:
            rows.append(current_row)
            current_row = [b]
if current_row:
    rows.append(current_row)

# Sort each row by X coordinate
final_bboxes = []
for row in rows:
    row.sort(key=lambda b: b[0])
    final_bboxes.extend(row)

out_dir = r"C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\res\drawable"
os.makedirs(out_dir, exist_ok=True)

filenames = [
    "illust_berry_red_round.png",
    "illust_berry_blue_round.png",
    "illust_berry_red_star.png",
    "illust_berry_blue_star.png",
    "illust_basket.png",
    "illust_nest.png"
]

if len(final_bboxes) != 6:
    print(f"ERROR: Expected 6 objects, found {len(final_bboxes)}.")
    # If auto-crop failed, try strict 2x3 or 3x2 grid crop
    if len(final_bboxes) < 6:
        print("Falling back to fixed 2x3 grid slicing...")
        final_bboxes = []
        h, w = img.shape[:2]
        # Assume 2 rows, 3 cols
        cell_h, cell_w = h // 2, w // 3
        for r in range(2):
            for c in range(3):
                # Add padding
                x1 = c * cell_w + 20
                y1 = r * cell_h + 20
                w1 = cell_w - 40
                h1 = cell_h - 40
                final_bboxes.append((x1, y1, w1, h1))

for i, bbox in enumerate(final_bboxes):
    if i >= 6: break
    x, y, w, h = bbox
    
    # Add some padding to the crop
    pad = 20
    x1, y1 = max(0, x - pad), max(0, y - pad)
    x2, y2 = min(img.shape[1], x + w + pad), min(img.shape[0], y + h + pad)
    
    crop = img[y1:y2, x1:x2]
    
    # Pad to square
    ch, cw = crop.shape[:2]
    size = max(ch, cw)
    square = np.zeros((size, size, 3), dtype=np.uint8)
    square[:] = bg_color
    
    off_y = (size - ch) // 2
    off_x = (size - cw) // 2
    square[off_y:off_y+ch, off_x:off_x+cw] = crop
    
    # Now apply the background removal script logic on the square
    h_sq, w_sq = square.shape[:2]
    mask = np.zeros((h_sq+2, w_sq+2), np.uint8)
    
    cv2.floodFill(square.copy(), mask, (0,0), (0,0,0), (10,10,10), (10,10,10), cv2.FLOODFILL_MASK_ONLY)
    
    bg_mask = mask[1:h_sq+1, 1:w_sq+1]
    
    bg_color_f = bg_color.astype(np.float32)
    diff_sq = np.sqrt(np.sum(np.square(square.astype(np.float32) - bg_color_f), axis=2))
    
    kernel = np.ones((5,5), np.uint8)
    dilated_bg = cv2.dilate(bg_mask, kernel, iterations=1)
    
    alpha = np.where(bg_mask == 1, 0, 255).astype(np.uint8)
    transition = (dilated_bg == 1) & (bg_mask == 0)
    
    soft_alpha = np.clip(diff_sq[transition] * (255.0 / 30.0), 0, 255).astype(np.uint8)
    alpha[transition] = soft_alpha
    alpha = cv2.GaussianBlur(alpha, (3,3), 0)
    
    img_rgba = cv2.cvtColor(square, cv2.COLOR_BGR2BGRA)
    img_rgba[:,:,3] = alpha
    
    # Resize to 512x512
    resized = cv2.resize(img_rgba, (512, 512), interpolation=cv2.INTER_AREA)
    
    out_path = os.path.join(out_dir, filenames[i])
    cv2.imwrite(out_path, resized)
    
    # Test composite to check for halos
    bg_test = np.zeros_like(resized)
    bg_test[:] = (255, 0, 0, 255) # Blue background
    test_alpha = resized[:, :, 3] / 255.0
    for c_idx in range(3):
        bg_test[:, :, c_idx] = (test_alpha * resized[:, :, c_idx] + (1 - test_alpha) * bg_test[:, :, c_idx])
    
    out_path_test = os.path.join(out_dir, f"test_{filenames[i]}")
    cv2.imwrite(out_path_test, bg_test)
    
    print(f"Processed {filenames[i]}, size: {resized.shape}, orig bbox: {w}x{h}")

print("Done.")
