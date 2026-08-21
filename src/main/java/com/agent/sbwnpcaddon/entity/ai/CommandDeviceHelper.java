package com.agent.sbwnpcaddon.entity.ai;

import net.minecraft.world.entity.Mob;

public class CommandDeviceHelper {
    public static void applyCommand(Mob mob, boolean patrolMode, double x1, double y1, double z1, double x2, double y2, double z2) {
        // Find existing SbwCommandGoal and update it, or add a new one
        SbwCommandGoal existing = null;
        for (net.minecraft.world.entity.ai.goal.WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof SbwCommandGoal goal) {
                existing = goal;
                break;
            }
        }

        if (existing == null) {
            existing = new SbwCommandGoal(mob);
            mob.goalSelector.addGoal(15, existing); // High priority (but lower than attack which is around 5)
        }

        existing.setCommand(patrolMode, x1, y1, z1, x2, y2, z2);
    }
}
