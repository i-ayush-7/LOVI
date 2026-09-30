import os
from PIL import Image

images = {
    'lovi_logo': r'C:\Users\Ayush Shukla\.gemini\antigravity\brain\ea37a3cb-0c7e-4770-a2cb-66aa2bfe2227\lovi_logo_1790504758733.jpg',
    'hero_banner': r'C:\Users\Ayush Shukla\.gemini\antigravity\brain\ea37a3cb-0c7e-4770-a2cb-66aa2bfe2227\hero_banner_v2_1790505195087.jpg',
    'icon_stay': r'C:\Users\Ayush Shukla\.gemini\antigravity\brain\ea37a3cb-0c7e-4770-a2cb-66aa2bfe2227\icon_stay_v2_1790505210948.jpg',
    'icon_notice': r'C:\Users\Ayush Shukla\.gemini\antigravity\brain\ea37a3cb-0c7e-4770-a2cb-66aa2bfe2227\icon_notice_v2_1790505223496.jpg',
    'icon_remember': r'C:\Users\Ayush Shukla\.gemini\antigravity\brain\ea37a3cb-0c7e-4770-a2cb-66aa2bfe2227\icon_remember_v2_1790505235033.jpg',
    'icon_wait': r'C:\Users\Ayush Shukla\.gemini\antigravity\brain\ea37a3cb-0c7e-4770-a2cb-66aa2bfe2227\icon_wait_v2_1790505246499.jpg',
    'icon_switch': r'C:\Users\Ayush Shukla\.gemini\antigravity\brain\ea37a3cb-0c7e-4770-a2cb-66aa2bfe2227\icon_switch_v2_1790505257332.jpg'
}

output_dir = r'C:\Users\Ayush Shukla\Downloads\Focus Builder\FocusBuilder\app\src\main\res\drawable'

for name, path in images.items():
    print(f'Processing {name} to PNG with alpha via PIL...')
    img = Image.open(path).convert("RGBA")
    if name != 'hero_banner':
        datas = img.getdata()
        newData = []
        for item in datas:
            gray = (item[0] + item[1] + item[2]) / 3.0
            if gray > 245:
                alpha = int(255 * (255 - gray) / 10.0) 
                newData.append((item[0], item[1], item[2], alpha))
            else:
                newData.append(item)
        img.putdata(newData)
    
    out_path = os.path.join(output_dir, f'{name}.png')
    img.save(out_path, format='PNG')
    print(f'Saved to {out_path}')
