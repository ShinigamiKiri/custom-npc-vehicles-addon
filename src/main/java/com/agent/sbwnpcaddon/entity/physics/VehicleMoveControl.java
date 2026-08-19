package com.agent.sbwnpcaddon.entity.physics;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.util.Mth;

public class VehicleMoveControl extends MoveControl {
    public float forwardIntent = 0.0F;
    public float sideIntent = 0.0F;

    public VehicleMoveControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        this.mob.setZza(0.0F);
        this.mob.setXxa(0.0F);
        this.mob.setYya(0.0F);

        if (this.operation == Operation.MOVE_TO) {
            double dx = this.wantedX - this.mob.getX();
            double dz = this.wantedZ - this.mob.getZ();
            double distanceSq = dx * dx + dz * dz;

            if (distanceSq < 4.0) {
                this.forwardIntent = 0.0F;
                this.sideIntent = 0.0F;
                return;
            }

            float targetYaw = (float)(Mth.atan2(dz, dx) * (double)(180F / (float)Math.PI)) - 90.0F;
            float yawDiff = Mth.wrapDegrees(targetYaw - this.mob.getYRot());

            if (yawDiff > 5.0F) {
                this.sideIntent = 1.0F;
            } else if (yawDiff < -5.0F) {
                this.sideIntent = -1.0F;
            } else {
                this.sideIntent = 0.0F;
            }

            if (Math.abs(yawDiff) > 60.0F) {
                this.forwardIntent = 0.3F;
            } else {
                this.forwardIntent = 1.0F;
            }
        } else {
            this.forwardIntent = 0.0F;
            this.sideIntent = 0.0F;
        }
    }
}
