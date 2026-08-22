package com.agent.sbwnpcaddon.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import java.util.EnumSet;

public class SbwCommandGoal extends Goal {
    private final Mob mob;
    private boolean active = false;
    private int mode = 0; // 0: Follow, 1: Stay, 2: Move, 3: Patrol, 4: Guard
    private double x1, y1, z1;
    private double x2, y2, z2;
    private boolean towardsPointB = false;
    
    private int followTeleportCooldown = 0;

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
        if (mode == 1 || mode == 4) {
            // For Stay and Guard, set current position as the stay point
            this.x1 = mob.getX();
            this.y1 = mob.getY();
            this.z1 = mob.getZ();
        }
    }

    public void deactivate() {
        this.active = false;
        mob.getNavigation().stop();
        if (mob.getMoveControl() instanceof com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl vmc) {
            vmc.forwardIntent = 0.0F;
            vmc.sideIntent = 0.0F;
        }
    }

    @Override
    public boolean canUse() {
        if (!active) return false;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return active;
    }

    @Override
    public void start() {
        navigate();
    }

    @Override
    public void tick() {
        if (mode == 0) { // Follow
            Player owner = getOwner();
            if (owner != null) {
                double distSq = mob.distanceToSqr(owner);
                if (followTeleportCooldown > 0) followTeleportCooldown--;
                
                // If extremely far or different dimension, teleport (fallback safety)
                if (owner.level() != mob.level() || distSq > 40000.0) { // 200 blocks
                    if (followTeleportCooldown <= 0) {
                        mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
                        followTeleportCooldown = 100;
                    }
                } else if (distSq > 64.0) { // More than 8 blocks away
                    mob.getNavigation().moveTo(owner, 1.0D);
                } else if (distSq < 16.0) { // Closer than 4 blocks
                    mob.getNavigation().stop();
                }
            }
        } else if (mode == 1 || mode == 4) { // Stay / Guard
            double distSq = mob.distanceToSqr(x1, y1, z1);
            int type = mob.getPersistentData().getInt("SbwVehicleType");
            boolean isPlane = (type == 2 || type == 3) && mob.getPersistentData().getInt("SbwAircraftMode") == 1;
            
            if (isPlane) {
                // Loiter behavior for planes
                if (distSq > 900.0) { // Further than 30 blocks from anchor, fly back to it
                    mob.getNavigation().moveTo(x1, y1, z1, 1.0D);
                } else {
                    // Inside loiter radius, circle it
                    mob.getNavigation().stop();
                    if (mob.getMoveControl() instanceof com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl vmc) {
                        vmc.forwardIntent = 1.0F;
                        vmc.sideIntent = 0.5F; // gentle turn to circle
                    }
                }
            } else {
                // Ground / Heli stay exactly
                if (distSq > 4.0) {
                    mob.getNavigation().moveTo(x1, y1, z1, 1.0D);
                } else {
                    mob.getNavigation().stop();
                    if (mob.getMoveControl() instanceof com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl vmc) {
                        vmc.forwardIntent = 0.0F;
                        vmc.sideIntent = 0.0F;
                    }
                }
            }
        } else { // Move or Patrol
            double targetX = towardsPointB ? x2 : x1;
            double targetY = towardsPointB ? y2 : y1;
            double targetZ = towardsPointB ? z2 : z1;

            double distSq = mob.distanceToSqr(targetX, targetY, targetZ);
            if (distSq < (isAircraft() ? 25.0 : 4.0)) {
                if (mode == 3) { // Patrol
                    towardsPointB = !towardsPointB;
                    navigate();
                } else { // Move
                    // Reached destination, turn into Stay mode
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
            mob.getNavigation().moveTo(targetX, targetY, targetZ, 1.0D);
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
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
