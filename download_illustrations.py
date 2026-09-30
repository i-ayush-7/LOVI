import urllib.request
import os

drawable_dir = "app/src/main/res/drawable"
os.makedirs(drawable_dir, exist_ok=True)

images = {
    "illust_welcome.png": "https://lh3.googleusercontent.com/aida/AEtjO1VmHqlKu3o2VE-fJJPogrhRISxWgnXOZatKouxe2pf9IwWkb5fCfxu8ZkmQhWqwDOqcbh1esG04fm34sJ8u2Khpphd9ypxkpe-6GMmX0PuS7DSFzJfDv92YyR_AHT7YDQhYzWnRiRWD0Z1zFpyR-KfJH4yghXi0Dnq_A36YjU9AMOM5QAGb76a_gKt43-Mp-gqBV2za5apZg1M8LZcjbLbnLUbYiCvyhK4o4vLALi2LJS8KA4hzCTJ3",
    "illust_game_intro.png": "https://lh3.googleusercontent.com/aida/AEtjO1VQy9VCYA4jS_lgUZh0sz4Vhn8lSOMhiuE962F_d5nuu6isa0smzgUzUo7yK0gxUcMU5zMoM6uO8f7znCwqZER2V-_BO1rsla4I8NtwOa6SAHge7ZuCZJWbYDRP_gacJgfaQXNLnxaXoIBQqOmAMdeu9g3zK-I4PbevuVDMXmUipjRl0OTPhGR4jX_OVW9rAXGhxHsGXJhIRMcjr2VFtufqiJkutuogTwZwACQadMIYkThWba9fe-PM6g",
    "illust_celebration.png": "https://lh3.googleusercontent.com/aida/AEtjO1UN0lqhHu7apH1nJ2riYAJ-BgzmUbEAmB0H1lsls6hgJdjCdh9xDOw10mtU4TzJNAMSSfTfXVngz2rjr8jp_HC-XB0XQAlH8yZaZgmIfSnn1O9VhsQ4AzHsMVEgZJ1bd5CJIOjpL-6dHQ3GMCHCXwnEtwQPSPumm_97gwbHaLSEugtGFP-tsVX70xPVCO9U5RHXX-_sxv50ZkcMx5tH3I1X7oYKi8J1VFMKL_Cnjyeun6agIVLRG7YymA",
    "illust_dashboard.png": "https://lh3.googleusercontent.com/aida/AEtjO1VMFJ-MlDXWImEqf4ZDzBEj2OW5RbrtZ2XwIepBeQwu1IDNB3MbR4I-wopvsJAm3w1juslljWtJ6bn44B7d4xDbBNma3xhOttIS_KyWKBF83rhbHqzsJ8FD-Gbqxvl9ftDeywUqY2HZc7C-y5oJrRuSYIJovQ3LcSTNFwxz5_DX7k_TcGrXsuykHcFX0XH4zH5c_OxXIyUaSNMUWtCeqRO4QGYVM9RPdaHOE81ez97-OyrgIkHUwrR9"
}

for name, url in images.items():
    filepath = os.path.join(drawable_dir, name)
    print(f"Downloading {name}...")
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response, open(filepath, 'wb') as out_file:
            out_file.write(response.read())
    except Exception as e:
        print(f"Failed to download {name}: {e}")

print("Done downloading illustrations.")
