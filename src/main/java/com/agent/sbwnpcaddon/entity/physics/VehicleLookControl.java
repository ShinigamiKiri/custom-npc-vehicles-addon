package com.agent.sbwnpcaddon.entity.physics;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class VehicleLookControl extends LookControl {
    public VehicleLookControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        // Do nothing to let the physics engine maintain strict rigid body rotation
    }
}
