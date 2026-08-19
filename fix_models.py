import os, json, glob
from PIL import Image
import re

geo_dir = 'src/main/resources/assets/sbw_npc_addon/geo/'
anim_dir = 'src/main/resources/assets/sbw_npc_addon/animations/'
tex_dir = 'src/main/resources/assets/sbw_npc_addon/textures/entity/'

for geo_path in glob.glob(geo_dir + '*.geo.json'):
    model_name = os.path.basename(geo_path).replace('.geo.json', '')
    
    with open(geo_path, 'r', encoding='utf-8') as f:
        geo_data = json.load(f)
        
    bones = []
    
    geos = geo_data.get('minecraft:geometry', [])
    if isinstance(geos, list):
        for g in geos:
            if 'bones' in g:
                for b in g['bones']:
                    bones.append(b['name'])

    # Fix texture UV sizes
    tex_path = os.path.join(tex_dir, model_name + '.png')
    modified_geo = False
    if os.path.exists(tex_path):
        with Image.open(tex_path) as img:
            w, h = img.size
            if isinstance(geos, list):
                for g in geos:
                    if 'description' in g:
                        desc = g['description']
                        geo_w = desc.get('texture_width', 0)
                        geo_h = desc.get('texture_height', 0)
                        if geo_w != w or geo_h != h:
                            desc['texture_width'] = w
                            desc['texture_height'] = h
                            modified_geo = True
    
    if modified_geo:
        with open(geo_path, 'w', encoding='utf-8') as f:
            json.dump(geo_data, f, indent=4)
        print(f"Fixed {model_name} texture size")
            
    # Fix animation mismatch
    anim_path = os.path.join(anim_dir, model_name + '.animation.json')
    if os.path.exists(anim_path):
        modified_anim = False
        with open(anim_path, 'r', encoding='utf-8') as f:
            anim_data = json.load(f)
        
        if 'animations' in anim_data:
            for anim_name, anim in anim_data['animations'].items():
                if 'bones' in anim:
                    bones_to_del = []
                    for b in anim['bones'].keys():
                        if b not in bones:
                            bones_to_del.append(b)
                    
                    for b in bones_to_del:
                        del anim['bones'][b]
                        modified_anim = True
        if modified_anim:
            with open(anim_path, 'w', encoding='utf-8') as f:
                json.dump(anim_data, f, indent=4)
            print(f"Fixed {model_name} animation bones")

# Fix renderer
renderer = 'src/main/java/com/agent/sbwnpcaddon/entity/client/SbwNpcRenderer.java'
with open(renderer, 'r', encoding='utf-8') as f:
    r_code = f.read()

r_code = re.sub(r'entityTranslucent', 'entityCutoutNoCull', r_code)
with open(renderer, 'w', encoding='utf-8') as f:
    f.write(r_code)

print("Fixed SbwNpcRenderer")
