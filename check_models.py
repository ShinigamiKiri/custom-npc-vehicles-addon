import os, json, glob
from PIL import Image

geo_dir = 'src/main/resources/assets/sbw_npc_addon/geo/'
anim_dir = 'src/main/resources/assets/sbw_npc_addon/animations/'
tex_dir = 'src/main/resources/assets/sbw_npc_addon/textures/entity/'

for geo_path in glob.glob(geo_dir + '*.geo.json'):
    model_name = os.path.basename(geo_path).replace('.geo.json', '')
    
    try:
        with open(geo_path, 'r', encoding='utf-8') as f:
            geo_data = json.load(f)
    except Exception as e:
        print(f"[{model_name}] Invalid JSON in geo file: {e}")
        continue
        
    bones = []
    parent_map = {}
    
    if 'minecraft:geometry' in geo_data:
        geos = geo_data['minecraft:geometry']
    else:
        geos = []

    if isinstance(geos, list):
        for g in geos:
            if 'bones' in g:
                for b in g['bones']:
                    bones.append(b['name'])
                    if 'parent' in b:
                        parent_map[b['name']] = b['parent']
                        
    for b, p in parent_map.items():
        if p not in bones:
            print(f"[{model_name}] RIGGING BUG: Bone '{b}' has missing parent '{p}'")
            
    anim_path = os.path.join(anim_dir, model_name + '.animation.json')
    if os.path.exists(anim_path):
        try:
            with open(anim_path, 'r', encoding='utf-8') as f:
                anim_data = json.load(f)
            
            if 'animations' in anim_data:
                for anim_name, anim in anim_data['animations'].items():
                    if 'bones' in anim:
                        for b in anim['bones'].keys():
                            if b not in bones:
                                print(f"[{model_name}] ANIMATION BUG: Animation '{anim_name}' references missing bone '{b}'")
        except Exception as e:
            print(f"[{model_name}] Invalid JSON in animation file: {e}")
            
    tex_path = os.path.join(tex_dir, model_name + '.png')
    if os.path.exists(tex_path):
        try:
            with Image.open(tex_path) as img:
                w, h = img.size
                
                if isinstance(geos, list):
                    for g in geos:
                        if 'description' in g:
                            desc = g['description']
                            geo_w = desc.get('texture_width', 0)
                            geo_h = desc.get('texture_height', 0)
                            if geo_w != w or geo_h != h:
                                print(f"[{model_name}] TEXTURE BUG: Geo expects {geo_w}x{geo_h} but texture is {w}x{h}")
        except Exception as e:
            pass

print('Done.')
