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
        
        // Persist command to NBT
        net.minecraft.nbt.CompoundTag data = mob.getPersistentData();
        data.putBoolean("SbwCommandActive", true);
        data.putBoolean("SbwCommandPatrol", patrolMode);
        data.putDouble("SbwCmdX1", x1); data.putDouble("SbwCmdY1", y1); data.putDouble("SbwCmdZ1", z1);
        data.putDouble("SbwCmdX2", x2); data.putDouble("SbwCmdY2", y2); data.putDouble("SbwCmdZ2", z2);
    }
    
    public static void cancelCommand(Mob mob) {
        mob.getPersistentData().putBoolean("SbwCommandActive", false);
        
        SbwCommandGoal existing = null;
        for (net.minecraft.world.entity.ai.goal.WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof SbwCommandGoal goal) {
                existing = goal;
                break;
            }
        }
        
        if (existing != null) {
            existing.deactivate();
            mob.goalSelector.removeGoal(existing);
        }
    }
    
    public static void ensureCommandRestored(Mob mob) {
        net.minecraft.nbt.CompoundTag data = mob.getPersistentData();
        if (data.getBoolean("SbwCommandActive")) {
            applyCommand(mob, 
                data.getBoolean("SbwCommandPatrol"), 
                data.getDouble("SbwCmdX1"), data.getDouble("SbwCmdY1"), data.getDouble("SbwCmdZ1"),
                data.getDouble("SbwCmdX2"), data.getDouble("SbwCmdY2"), data.getDouble("SbwCmdZ2")
            );
        }
    }
}
