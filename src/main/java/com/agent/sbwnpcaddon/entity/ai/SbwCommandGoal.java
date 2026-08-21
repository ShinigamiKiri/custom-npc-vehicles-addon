package com.agent.sbwnpcaddon.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import java.util.EnumSet;

public class SbwCommandGoal extends Goal {
    private final Mob mob;
    private boolean active = false;
    private boolean patrolMode = false;
    private double x1, y1, z1;
    private double x2, y2, z2;
    private boolean towardsPointB = false;

    public SbwCommandGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public void setCommand(boolean patrolMode, double x1, double y1, double z1, double x2, double y2, double z2) {
        this.active = true;
        this.patrolMode = patrolMode;
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        this.x2 = x2; this.y2 = y2; this.z2 = z2;
        this.towardsPointB = false;
    }

    @Override
    public boolean canUse() {
        if (!active) return false;

        Player owner = getOwner();
        if (owner == null) {
            active = false;
            return false;
        }

        // Must be holding the command device
        if (!(owner.getMainHandItem().getItem() instanceof com.agent.sbwnpcaddon.item.CommandDeviceItem) &&
            !(owner.getOffhandItem().getItem() instanceof com.agent.sbwnpcaddon.item.CommandDeviceItem)) {
            active = false; // Disable if they stop holding it
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        navigate();
    }

    @Override
    public void tick() {
        double targetX = towardsPointB ? x2 : x1;
        double targetY = towardsPointB ? y2 : y1;
        double targetZ = towardsPointB ? z2 : z1;

        double distSq = mob.distanceToSqr(targetX, targetY, targetZ);
        if (distSq < 2.0) {
            if (patrolMode) {
                towardsPointB = !towardsPointB;
                navigate();
            } else {
                mob.getNavigation().stop();
                // Stay at destination
            }
        } else if (mob.getNavigation().isDone()) {
            navigate();
        }
    }

    private void navigate() {
        double targetX = towardsPointB ? x2 : x1;
        double targetY = towardsPointB ? y2 : y1;
        double targetZ = towardsPointB ? z2 : z1;
        mob.getNavigation().moveTo(targetX, targetY, targetZ, 1.0D);
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
            }
        } catch (Exception e) {}
        return null;
    }
}
