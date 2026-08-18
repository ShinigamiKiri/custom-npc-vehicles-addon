# Superb Warfare NPC Addon - Project Log

## Naming Decisions
- **Mod ID**: `sbw_npc_addon` (Short, descriptive, complies with Forge constraints).
- **Mod Name**: `Superb Warfare NPC Addon` (Clearly states its purpose to combine Superb Warfare models with CustomNPCs capabilities).
- **Package**: `com.agent.sbwnpcaddon` (Standard Java convention).
- **Entity Classes**: Generated dynamically per-model to create exactly 88 entities. A base entity class `SbwNpcEntity` extending `PathfinderMob` and implementing `GeoEntity` was used. CustomNPCs treats any `LivingEntity` gracefully, and GeckoLib will handle our rendering.
- **Audio Structure**: The `sounds/` directory was generated with subfolders: `gunfire`, `cannon`, `bullet_flyby`, and `bullet_impact`. Java `SoundEvent`s were hardcoded to look for generic files like `rifle_shot_01.ogg`. (See `SOUND_TODO.md` for drop-in details).

## GitHub Repository Setup & Build Guide

The project has now been fully initialized and pushed to GitHub!

- **Remote URL**: `https://github.com/ShinigamiKiri/custom-npc-vehicles-addon`
- **Latest Commit Hash**: `008fe84149149f9055bb0d0b38b7fab0b61ac4fb`

### How to Trigger the Actions Build
The `.github/workflows/build.yml` file is automatically configured to build your Forge mod using GitHub's servers and the embedded local `geckolib-forge` jar. Since the push to the `main` branch just happened, it should already be building!
If you ever need to trigger it manually:
1. Go to the [GitHub Repository page](https://github.com/ShinigamiKiri/custom-npc-vehicles-addon).
2. Click on the **Actions** tab at the top.
3. On the left sidebar, click on **Build Forge Mod**.
4. Click the **Run workflow** dropdown button on the right side of the screen.
5. Select the `main` branch and click the green **Run workflow** button.

### How to Download the Artifact
1. Once the workflow run finishes successfully (you will see a green checkmark next to it in the Actions tab), click on that specific run.
2. Scroll to the very bottom of the page to the **Artifacts** section.
3. Click on the `sbw_npc_addon-jar` artifact link.
4. It will download a zip file containing the fully compiled `.jar` file that is ready to be dragged into your `mods/` folder!

## GeckoLib 4.7 API Verification Note
- **Correction:** The user provided the explicit `geckolib-forge-1.20.1-4.7.jar` locally. I re-scanned the directory, extracted the jar, and mapped its API signatures. 
- **Verification:** I verified that `GeoEntity`, `GeoModel`, `GeoEntityRenderer`, `GeckoLibUtil`, and `AnimatableInstanceCache` exactly match the 4.4 assumptions. 
- **Build Update:** I updated `build.gradle` to map the explicit file path (`libs/geckolib-forge-1.20.1-4.7.jar`) instead of the generic `software.bernie.geckolib` Maven repository. All 88 generated entity classes correctly compile and bind to this API natively!
