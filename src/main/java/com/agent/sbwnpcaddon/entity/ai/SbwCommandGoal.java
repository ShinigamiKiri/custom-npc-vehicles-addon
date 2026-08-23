package com.agent.sbwnpcaddon.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumSet;

public class SbwCommandGoal extends Goal {
    private final Mob mob;
    private boolean active = false;
    private int mode = 0; // 0: Follow, 1: Stay, 2: Move, 3: Patrol, 4: Guard, 5: Patrol-Guard
    private double x1, y1, z1;
    private double x2, y2, z2;
    private boolean towardsPointB = false;
    
    private int followTeleportCooldown = 0;
    
    // Combat State Tracking for Presets
    private boolean yieldedForCombat = false;
    private int combatTickCounter = 0;
    private double lastCombatX, lastCombatY, lastCombatZ;
    private int stationaryTicks = 0;
    private static final int MAX_COMBAT_TICKS = 15 * 20; // 15 seconds tunable

    public SbwCommandGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public void setCommand(int mode, double x1, double y1, double z1, double x2, double y2, double z2) {
        this.active = true;
        this.mode = mode;
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        this.x2 = x2; this.y2 = y2; this.z2 = z2;
        this.towardsPointB = false;
        this.yieldedForCombat = false;
        if (mode == 1 || mode == 4) {
            this.x1 = mob.getX();
            this.y1 = mob.getY();
            this.z1 = mob.getZ();
        }
    }

    public void deactivate() {
        this.active = false;
        this.yieldedForCombat = false;
        mob.getPersistentData().putBoolean("SbwForceOwnerAssist", false);
        mob.getPersistentData().putBoolean("SbwPrioritizeSelfDefense", false);
        mob.getNavigation().stop();
        if (mob.getMoveControl() instanceof com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl vmc) {
            vmc.forwardIntent = 0.0F;
            vmc.sideIntent = 0.0F;
        }
    }

    private boolean shouldYieldToCombat() {
        if (mode == 1) return false; // Stay mode never yields

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        // Forced assist or self-defense overrides preset logic
        if (mob.getPersistentData().getBoolean("SbwForceOwnerAssist") || mob.getPersistentData().getBoolean("SbwPrioritizeSelfDefense")) {
            return true;
        }

        int preset = mob.getPersistentData().contains("SbwCombatPreset") ? mob.getPersistentData().getInt("SbwCombatPreset") : 1;
        
        if (preset == 1) { // Proximity Engage
            if (mode == 0 || mode == 4 || mode == 5) {
                // Guard/Patrol-Guard/Follow modes proactively engage any hostile within their native aggro range
                return true; 
            } else if (mode == 2) {
                // Move mode: engage hostiles near destination or current position
                return mob.distanceToSqr(target) <= 100.0 || target.distanceToSqr(x1, y1, z1) <= 100.0;
            } else if (mode == 3) {
                // Patrol mode: engage hostiles near the mob along the path
                return mob.distanceToSqr(target) <= 100.0;
            }
        } else if (preset == 2) { // Retaliate on Aggro
            LivingEntity targetOfTarget = null;
            if (target instanceof Mob m) {
                targetOfTarget = m.getTarget();
            } else if (target instanceof net.minecraft.world.entity.monster.Creeper) {
                targetOfTarget = ((net.minecraft.world.entity.monster.Creeper)target).getTarget();
            }
            return targetOfTarget == mob;
        } else if (preset == 3) { // Self-Defense Only (Tank)
            return false; // Never proactively yields MOVE
        }
        
        return false;
    }

    @Override
    public boolean canUse() {
        if (!active) return false;

        if (mode == 1) return true; // Stay mode never yields

        if (yieldedForCombat) {
            LivingEntity target = mob.getTarget();
            if (target == null || !target.isAlive()) {
                yieldedForCombat = false;
                mob.getPersistentData().putBoolean("SbwForceOwnerAssist", false);
                mob.getPersistentData().putBoolean("SbwPrioritizeSelfDefense", false);
                return true; // Target lost/dead, reclaim MOVE
            }

            int preset = mob.getPersistentData().contains("SbwCombatPreset") ? mob.getPersistentData().getInt("SbwCombatPreset") : 1;
            
            if (preset == 1 || mob.getPersistentData().getBoolean("SbwForceOwnerAssist") || mob.getPersistentData().getBoolean("SbwPrioritizeSelfDefense")) {
                combatTickCounter++;
                double distMovedSq = mob.distanceToSqr(lastCombatX, lastCombatY, lastCombatZ);
                if (distMovedSq < 1.0) {
                    stationaryTicks++;
                } else {
                    stationaryTicks = 0;
                    lastCombatX = mob.getX();
                    lastCombatY = mob.getY();
                    lastCombatZ = mob.getZ();
                }

                if (combatTickCounter > MAX_COMBAT_TICKS || stationaryTicks > MAX_COMBAT_TICKS) {
                    // Forcibly abandon fight
                    mob.setTarget(null); // Clear target so native attack stops
                    yieldedForCombat = false;
                    mob.getPersistentData().putBoolean("SbwForceOwnerAssist", false);
                    mob.getPersistentData().putBoolean("SbwPrioritizeSelfDefense", false);
                    return true;
                }
            } else if (preset == 2) {
                // If the target stops targeting us, reclaim MOVE
                LivingEntity targetOfTarget = (target instanceof Mob m) ? m.getTarget() : null;
                if (targetOfTarget != mob) {
                    mob.setTarget(null);
                    yieldedForCombat = false;
                    return true;
                }
            }
            
            return false; // Continue yielding
        } else {
            if (shouldYieldToCombat()) {
                yieldedForCombat = true;
                combatTickCounter = 0;
                stationaryTicks = 0;
                lastCombatX = mob.getX();
                lastCombatY = mob.getY();
                lastCombatZ = mob.getZ();
                return false; // Yield now
            }
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        if (!yieldedForCombat) {
            navigate();
        }
    }

    @Override
    public void tick() {
        if (yieldedForCombat) return;

        if (mode == 0) { // Follow
            Player owner = getOwner();
            if (owner != null) {
                double distSq = mob.distanceToSqr(owner);
                if (followTeleportCooldown > 0) followTeleportCooldown--;
                
                if (this.mode == 0 && (owner.level() != mob.level() || distSq > 40000.0)) {
                    if (followTeleportCooldown <= 0) {
                        mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
                        followTeleportCooldown = 100;
                    }
                } else {
                    if (isAircraft()) {
                        mob.getMoveControl().setWantedPosition(owner.getX(), owner.getY(), owner.getZ(), 1.0D);
                    } else {
                        if (distSq > 64.0) {
                            mob.getNavigation().moveTo(owner, 1.0D);
                        } else if (distSq < 16.0) {
                            mob.getNavigation().stop();
                        }
                    }
                }
            }
        } else if (mode == 1) { // Stay
            mob.getNavigation().stop();
            if (mob.getMoveControl() instanceof com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl vmc) {
                vmc.forwardIntent = 0.0F;
                vmc.sideIntent = 0.0F;
                if (isAircraft()) {
                    vmc.wantedY = mob.getY() - 10.0; // Force descent to land
                }
            }
        } else if (mode == 4) { // Guard
            double distSq = mob.distanceToSqr(x1, y1, z1);
            int type = mob.getPersistentData().getInt("SbwVehicleType");
            boolean isPlane = (type == 2 || type == 3) && mob.getPersistentData().getInt("SbwAircraftMode") == 1;
            
            if (isPlane) {
                if (distSq > 900.0) {
                    mob.getMoveControl().setWantedPosition(x1, y1, z1, 1.0D);
                } else {
                    mob.getNavigation().stop();
                    if (mob.getMoveControl() instanceof com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl vmc) {
                        vmc.forwardIntent = 1.0F;
                        vmc.sideIntent = 0.5F;
                    }
                }
            } else {
                if (distSq > 4.0) {
                    if (isAircraft()) {
                        mob.getMoveControl().setWantedPosition(x1, y1, z1, 1.0D);
                    } else {
                        mob.getNavigation().moveTo(x1, y1, z1, 1.0D);
                    }
                } else {
                    mob.getNavigation().stop();
                    if (mob.getMoveControl() instanceof com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl vmc) {
                        vmc.forwardIntent = 0.0F;
                        vmc.sideIntent = 0.0F;
                    }
                }
            }
        } else if (mode == 5) { // Patrol-Guard
            Player owner = getOwner();
            if (owner != null) {
                double distSq = mob.distanceToSqr(owner);
                if (distSq > 400.0) { // Outside roam radius, move to player
                    if (isAircraft()) {
                        mob.getMoveControl().setWantedPosition(owner.getX(), owner.getY(), owner.getZ(), 1.0D);
                    } else {
                        mob.getNavigation().moveTo(owner, 1.0D);
                    }
                } else {
                    // Wander inside radius
                    if (mob.getNavigation().isDone() || mob.getRandom().nextInt(100) == 0) {
                        double rx = owner.getX() + (mob.getRandom().nextDouble() - 0.5) * 30.0;
                        double rz = owner.getZ() + (mob.getRandom().nextDouble() - 0.5) * 30.0;
                        if (isAircraft()) {
                            double ry = owner.getY() + 10.0 + (mob.getRandom().nextDouble() - 0.5) * 10.0;
                            mob.getMoveControl().setWantedPosition(rx, ry, rz, 1.0D);
                        } else {
                            mob.getNavigation().moveTo(rx, owner.getY(), rz, 1.0D);
                        }
                    }
                }
            }
        } else { // Move or Patrol
            double targetX = towardsPointB ? x2 : x1;
            double targetY = towardsPointB ? y2 : y1;
            double targetZ = towardsPointB ? z2 : z1;

            double distSq = mob.distanceToSqr(targetX, targetY, targetZ);
            if (distSq < (isAircraft() ? 25.0 : 4.0)) {
                if (mode == 3) {
                    towardsPointB = !towardsPointB;
                    navigate();
                } else {
                    mode = 1;
                    this.x1 = targetX; this.y1 = targetY; this.z1 = targetZ;
                }
            } else if (mob.getNavigation().isDone()) {
                navigate();
            }
        }
    }
    
    private boolean isAircraft() {
        int type = mob.getPersistentData().getInt("SbwVehicleType");
        return type == 2 || type == 3;
    }

    private void navigate() {
        if (mode == 2 || mode == 3) {
            double targetX = towardsPointB ? x2 : x1;
            double targetY = towardsPointB ? y2 : y1;
            double targetZ = towardsPointB ? z2 : z1;
            if (isAircraft()) {
                mob.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0D);
            } else {
                mob.getNavigation().moveTo(targetX, targetY, targetZ, 1.0D);
            }
        }
    }

    @Override
    public void stop() {
        if (!yieldedForCombat) {
            mob.getNavigation().stop();
        }
    }

    private Player getOwner() {
        try {
            java.lang.reflect.Method getOwner = mob.getClass().getMethod("getOwner");
            Object owner = getOwner.invoke(mob);
            if (owner instanceof Player p) {
                return p;
            } else if (owner instanceof Entity e) {
                return mob.level().getPlayerByUUID(e.getUUID());
            }
        } catch (Exception e) {}
        return null;
    }
}
