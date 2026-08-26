# Antigravity Rules: Protected Invariants

Before implementing any new feature or fix in this project, you must read this rules file and explicitly check whether your proposed changes risk violating any of these protected invariants. If a change does risk violating them, you must explicitly call it out to the user before proceeding, rather than silently regressing something.

## Protected Invariants

1. **NBT Persistence**: All vehicle-specific NBT tags must be persisted via `addAdditionalSaveData` and `readAdditionalSaveData` directly to the root NBT, NEVER to `PersistentData`. This is specifically because Custom NPCs' clone wand strips `PersistentData` during cloning. This invariant must be verified and re-tested after any change touching NBT persistence or the entity classes.
   The comprehensive list of protected NBT tags includes:
   - `SbwVehicleType`
   - `SbwMaxSpeed`
   - `SbwAcceleration`
   - `SbwBraking`
   - `SbwTurnRadius`
   - `SbwPhysicsEnabled`
   - `SbwAircraftMode`
   - `SbwCommandActive`
   - `SbwCommandMode`
   - `SbwPrioritizeSelfDefense`
   - `SbwForceOwnerAssist`
   - `SbwSeatOffset`
   - `SbwCommandPatrol`
   - `SbwCombatPreset`
   - `SbwModelYawOffset`
   - `SbwProjectileLoadout` (ListTag of projectile setups)
   - `SbwActiveProjectileIndex` (Int index into the loadout)

2. **Physics Authority**: `SbwPhysicsModule` and `SbwCommandGoal` must remain the single sole authority over `yRot`, `yBodyRot`, `yHeadRot`, and movement velocity for physics-enabled vehicles. No other goal or vanilla system may write to these properties.

3. **Goal Selector Conflicts**: The priority-based Goal Selector conflict-avoidance pattern, which is used to safely coexist with Custom NPCs' native AI, must be preserved at all times.

4. **Aircraft Cross-Contamination**: Any change to aircraft physics, animation, or rendering must be manually re-tested against both ground-vehicle and helicopter behavior to confirm no cross-contamination. This project has a history of aircraft-specific fixes accidentally breaking other vehicle types.

5. **Build Verification**: Every task must end with a real pushed build verified green via GitHub Actions with a non-empty JAR artifact confirmed via the API. Never rely on just a code-review claim of success.
