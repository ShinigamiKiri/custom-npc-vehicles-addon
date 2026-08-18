# Superb Warfare NPC Addon - How to Use

## What is this mod?
The **Superb Warfare NPC Addon** is a companion mod designed to integrate 3D models from the Superb Warfare mod into your Minecraft world. It leverages GeckoLib to render high-quality models (such as guns, tanks, helicopters, and artillery) and registers them as standard Minecraft entities. 

This means you can summon them like regular mobs or, more importantly, use them as highly detailed model overrides for your **CustomNPCs**!

---

## 📦 Required Dependencies
To run this mod on **Minecraft 1.20.1**, you must install the following dependencies in your `mods` folder:
1. **[Minecraft Forge (1.20.1 - version 47+)](https://files.minecraftforge.net/)**
2. **[GeckoLib (1.20.1 - version 4.4+)](https://www.curseforge.com/minecraft/mc-mods/geckolib)**
3. **[CustomNPCs (1.20.1)](https://www.curseforge.com/minecraft/mc-mods/custom-npcs)** (The unofficial 1.20.1 GBPort provided in your folder)

---

## 🛠️ Installation Instructions
1. Ensure you have the **Forge 1.20.1** profile installed in your Minecraft Launcher.
2. Press `Win + R`, type `%appdata%\.minecraft\mods`, and hit Enter to open your `mods` folder. (Or navigate to the `mods` folder of your specific CurseForge/Prism/Modrinth instance).
3. Drop the following `.jar` files into the `mods` folder:
   - `sbw_npc_addon-1.0.0.jar`
   - `CustomNPCs-1.20.1-GBPort-Unofficial-20251031.jar`
   - `geckolib-forge-1.20.1-4.7.jar` (or equivalent supported version)
4. Launch the game!

---

## 🎮 How to Use in Game

Because this mod registers these models as standard entities, you can interact with them in two primary ways:

### Method 1: Using CustomNPCs (Recommended)
You can apply these highly detailed 3D models to your CustomNPCs to create stationary gun emplacements, parked vehicles, or custom decorations!
1. Get an **NPC Wand** and right-click the ground to create a new NPC.
2. In the CustomNPC menu, go to the **Display** tab.
3. Click on the **Model** button.
4. Scroll through the Entity list and select one of the `sbw_npc_addon:` entities (e.g., `sbw_npc_addon:ak_47` or `sbw_npc_addon:prism_tank`).
5. Close the menu, and your NPC will now look exactly like the selected Superb Warfare model!

### Method 2: Summon Commands
Since the mod does not include spawn eggs, you can spawn the raw entities directly into the world using vanilla Minecraft commands:
```mcfunction
/summon sbw_npc_addon:<entity_id>
```
*Example: `/summon sbw_npc_addon:drone`*

*(Note: Without CustomNPCs governing them, these entities will just stand still as they have no custom AI of their own.)*

---

## 📋 Full List of Custom Entities
Here are all 101 custom entity IDs added by this mod. Use these exactly as written for `/summon` commands, or look for them in the CustomNPCs model list.

- `aa_12`
- `ah_6`
- `ak_12`
- `ak_47`
- `annihilator`
- `awm`
- `a_10a`
- `bl_132`
- `bmp_2`
- `bocek`
- `bradley`
- `container`
- `devotion`
- `drone`
- `glock_17`
- `glock_18`
- `gun_mu`
- `hk_416`
- `homemade_shotgun`
- `hpj_11`
- `hunting_rifle`
- `igla_9k38`
- `insidious`
- `javelin`
- `ju_87`
- `kv_16`
- `k_98`
- `laser_tower`
- `lav_150`
- `lav_25`
- `lav_ad`
- `lunge_mine`
- `m79`
- `marlin`
- `military_shovel`
- `minigun`
- `mi_28`
- `mk_14`
- `mk_42`
- `mle_1934`
- `mortar`
- `mosin_nagant`
- `mp_443`
- `mp_5`
- `m_1911`
- `m_1a_2`
- `m_2_hb`
- `m_4`
- `m_60`
- `m_870`
- `m_98b`
- `ntw_20`
- `plz_05`
- `prism_tank`
- `ptkm_1r_item`
- `qbz_191`
- `qbz_95`
- `ql_1031`
- `repair_tool`
- `rpg`
- `rpk`
- `secondary_cataclysm`
- `sentinel`
- `sks`
- `small_container`
- `sodayo_pick_up`
- `sodayo_pick_up_hmg`
- `sodayo_pick_up_rocket`
- `sodayo_pick_up_tow`
- `speedboat`
- `super_star_shooter`
- `svd`
- `taser`
- `tiny_speedboat`
- `tm_62`
- `tom_6`
- `tow`
- `trachelium`
- `truck`
- `type_63`
- `t_90a`
- `vector`
- `vehicle_assembling_table`
- `vehicle_assembling_table_vehicle`
- `waveforce_tower`
- `wheel_chair`
- `yx_100`
- `ztz_99a`

**Immersive Vehicles Models (.obj):**
- `iv_charger`
- `iv_custombench`
- `iv_decorbench`
- `iv_enginebench`
- `iv_fuelpump`
- `iv_gunbench`
- `iv_instrumentbench`
- `iv_invisible`
- `iv_itembench`
- `iv_jerrycan`
- `iv_propellerbench`
- `iv_seatbench`
- `iv_vehiclebench`
- `iv_wheelbench`

---

## 🔊 Important Note on the Sound System
The mod has a fully implemented sound registry (e.g., player gunfire, cannon blasts, bullet impacts), but **the custom `.ogg` sound files themselves are currently missing/placeholders.**

If you want to hear these custom sounds in-game, you must manually provide `.ogg` audio files and place them into the correct directory paths within the mod's source or via a resource pack. 

Please refer to the `SOUND_TODO.md` file in the project folder for the exact folder structures and file names required (e.g., `rifle_shot_01.ogg`, `cannon_fire_01.ogg`).
