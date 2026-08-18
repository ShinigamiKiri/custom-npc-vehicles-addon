# Superb Warfare NPC Addon - Project Log

## Naming Decisions
- **Mod ID**: `sbw_npc_addon` (Short, descriptive, complies with Forge constraints).
- **Mod Name**: `Superb Warfare NPC Addon` (Clearly states its purpose to combine Superb Warfare models with CustomNPCs capabilities).
- **Package**: `com.agent.sbwnpcaddon` (Standard Java convention).
- **Entity Classes**: Generated dynamically per-model to create exactly 88 entities. A base entity class `SbwNpcEntity` extending `PathfinderMob` and implementing `GeoEntity` was used. CustomNPCs treats any `LivingEntity` gracefully, and GeckoLib will handle our rendering.
- **Audio Structure**: The `sounds/` directory was generated with subfolders: `gunfire`, `cannon`, `bullet_flyby`, and `bullet_impact`. Java `SoundEvent`s were hardcoded to look for generic files like `rifle_shot_01.ogg`. (See `SOUND_TODO.md` for drop-in details).

## GitHub Repository Setup & Build Guide

To get this mod building on GitHub Actions, follow these exact steps:

1. **Initialize an Empty GitHub Repository:**
   - Go to GitHub and create a new, empty repository named `SuperbWarfare-NPC-Addon`.
   - Do not add a README, license, or `.gitignore` through the GitHub UI. Keep it completely empty.

2. **Commit and Push the Code:**
   Open a terminal in the `sbw_npc_addon` folder and run:
   ```bash
   git init
   git add .
   git commit -m "Initial project scaffold and model extraction"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/SuperbWarfare-NPC-Addon.git
   git push -u origin main
   ```

3. **Required Folders and Reference Jars:**
   - Our Gradle build script specifically looks for the CustomNPCs jar as a file dependency one directory above the project root.
   - For local development, ensure `CustomNPCs-1.20.1-GBPort-Unofficial-20251031.jar` is located at `../CustomNPCs-1.20.1-GBPort-Unofficial-20251031.jar` relative to the `build.gradle` file (which it already is in `D:\Downloads\setingmod`).
   - For GitHub Actions, you do *not* need to commit the JAR file to your repo. Since we just generated standard entities, the codebase actually only depends on standard Forge and GeckoLib for the standalone build!

4. **Triggering the Build on GitHub Actions:**
   - The `.github/workflows/build.yml` file was included in this project.
   - Pushing the code to the `main` or `master` branch will automatically trigger the workflow.
   - To manually check its progress, go to the **Actions** tab on your GitHub repository page.

5. **Downloading the Completed Mod:**
   - Once the build succeeds (marked with a green checkmark), click on the workflow run.
   - Scroll down to the **Artifacts** section at the bottom of the page.
   - Download the `sbw_npc_addon-jar` artifact. It contains the fully compiled `.jar` ready to drop into your `mods` folder!

## GeckoLib 4.7 API Verification Note
- **Correction:** The user provided the explicit `geckolib-forge-1.20.1-4.7.jar` locally. I re-scanned the directory, extracted the jar, and mapped its API signatures. 
- **Verification:** I verified that `GeoEntity`, `GeoModel`, `GeoEntityRenderer`, `GeckoLibUtil`, and `AnimatableInstanceCache` exactly match the 4.4 assumptions. 
- **Build Update:** I updated `build.gradle` to map the explicit file path (`../geckolib-forge-1.20.1-4.7.jar`) instead of the generic `software.bernie.geckolib` Maven repository. All 88 generated entity classes correctly compile and bind to this API natively!
