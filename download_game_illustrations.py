import urllib.request
from PIL import Image
import os

drawable_dir = "app/src/main/res/drawable"
os.makedirs(drawable_dir, exist_ok=True)

images = {
    "illust_lantern_go.png": "https://lh3.googleusercontent.com/aida/AEtjO1Uq_Z4ZUysDnhDmHtJyYTzmI68FqyCr3GEsaSFahcPFTQRq6t1KCsdPNCqPjfgPOalhO8wPg4YKKHqo9FopJmMLybBpYpI3Lr3WciaqoEtNfwELodzKid_wuMKPz55gNmqQHHKUk-IJ2c-g_PoGUrLk85O6I1p1U_pLqd5sOwx1I-pHuZTlnxu5h8dtYX17C5hTv6nQcMYUBgcRjkVB8JRuWWqUlHlb87vC1FBWpgAKmyQiKZyxpvqMKg",
    "illust_lantern_stop.png": "https://lh3.googleusercontent.com/aida/AEtjO1WteSuzYC8ZJOyb0UZKXy23vqffANeJZHmypw8GFiLZ6QVqgtyF3b8C2GH8kg7MEFvdN7mUloZeYcAZiuyK2syfuCuQFbGe4QpAwdygU_GJyqLMA1BUNWnBSsH36RdBsnDlO6cbQaqkR7xK7q9hu25MWzTATLdNQhYWbrjp80lcEVyHgF4QlFWvzq_KUxf8JzO8V8lWe1te7fZlAAfbFw_cAgsFX-pC0kTu6LbiTqnaJfxQIrbhBehwrw"
}

for name, url in images.items():
    filepath = os.path.join(drawable_dir, name)
    print(f"Downloading {name}...")
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response, open(filepath, 'wb') as out_file:
            out_file.write(response.read())
            
        # Resize to 512x512 with LANCZOS and optimize
        with Image.open(filepath) as img:
            img = img.resize((512, 512), Image.Resampling.LANCZOS)
            img.save(filepath, optimize=True, quality=85)
            print(f"Resized and optimized {name}")
    except Exception as e:
        print(f"Failed to download/resize {name}: {e}")

print("Done downloading game illustrations.")
