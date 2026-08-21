package com.agent.sbwnpcaddon.entity.physics;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.util.Mth;

public class VehicleMoveControl extends MoveControl {
    public float forwardIntent = 0.0F;
    public float sideIntent = 0.0F;

    public double wantedY;

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
            this.wantedY = super.wantedY; // Track height for helicopters
            double dz = this.wantedZ - this.mob.getZ();
            double distanceSq = dx * dx + dz * dz;

            int type = this.mob.getPersistentData().getInt("SbwVehicleType");
            boolean isAircraft = (type == 2 || type == 3);

            double dy = this.wantedY - this.mob.getY();
            if (isAircraft) {
                distanceSq += dy * dy;
            }

            if (distanceSq < (isAircraft ? 9.0 : 0.25)) { // 3 blocks for aircraft, 0.5 blocks for ground
                this.forwardIntent = 0.0F;
                this.sideIntent = 0.0F;
                this.operation = Operation.WAIT; // Mark operation as done
                return;
            }

            float targetYaw = (float)(Mth.atan2(dz, dx) * (double)(180F / (float)Math.PI)) - 90.0F;
            float yawDiff = Mth.wrapDegrees(targetYaw - this.mob.getYRot());

            // Smooth proportional steering to eliminate oscillation
            this.sideIntent = Mth.clamp(yawDiff / 30.0F, -1.0F, 1.0F);

            if (!isAircraft && Math.abs(yawDiff) > 60.0F) {
                this.forwardIntent = 0.3F; // slow down in tight turns for ground vehicles
            } else {
                this.forwardIntent = 1.0F;
            }
        } else {
            this.forwardIntent = 0.0F;
            this.sideIntent = 0.0F;
        }
    }
}
