import cv2
import numpy as np
import os

img_path = r"C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\res\drawable\illust_fox.png"
img = cv2.imread(img_path, cv2.IMREAD_UNCHANGED)

bg = np.zeros_like(img)
bg[:] = (255, 0, 0, 255) # Blue background

alpha = img[:, :, 3] / 255.0
for c in range(3):
    bg[:, :, c] = (alpha * img[:, :, c] + (1 - alpha) * bg[:, :, c])

out_path = r"C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\res\drawable\test_fox_bg.png"
cv2.imwrite(out_path, bg)
print("Saved composite image")
