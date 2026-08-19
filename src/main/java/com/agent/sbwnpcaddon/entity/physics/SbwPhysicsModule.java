package com.agent.sbwnpcaddon.entity.physics;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SbwPhysicsModule {
    private final Mob entity;
    
    private final Quaternionf rotation = new Quaternionf();
    private float hullYaw = 0.0f;
    private float pitch = 0.0f;
    private float roll = 0.0f;
    
    private double currentSpeed = 0.0;
    private Vec3 velocity = Vec3.ZERO;
    
    public SbwPhysicsModule(Mob entity) {
        this.entity = entity;
        this.hullYaw = entity.getYRot();
    }
    
    public void tick() {
        int type = entity.getPersistentData().getInt("SbwVehicleType");
        
        float maxSpeed = entity.getPersistentData().contains("SbwMaxSpeed") ? entity.getPersistentData().getFloat("SbwMaxSpeed") : 0.5f;
        float acceleration = entity.getPersistentData().contains("SbwAcceleration") ? entity.getPersistentData().getFloat("SbwAcceleration") : 0.005f;
        float braking = entity.getPersistentData().contains("SbwBraking") ? entity.getPersistentData().getFloat("SbwBraking") : 0.02f;
        float turnRadius = entity.getPersistentData().contains("SbwTurnRadius") ? entity.getPersistentData().getFloat("SbwTurnRadius") : 1.0f;
        
        float forwardInput = entity.zza; // forward/backward
        float sideInput = entity.xxa; // left/right
        
        if (entity.getMoveControl() instanceof VehicleMoveControl vmc) {
            forwardInput = vmc.forwardIntent;
            sideInput = vmc.sideIntent;
        }
        
        if (type == 0 || type == 1) { // Ground or Boat
            if (forwardInput > 0) {
                if (currentSpeed < maxSpeed) currentSpeed += acceleration;
            } else if (forwardInput < 0) {
                currentSpeed -= braking;
                if (currentSpeed < -maxSpeed / 2) currentSpeed = -maxSpeed / 2;
            } else {
                currentSpeed *= 0.90; 
                if (currentSpeed < 0.01 && currentSpeed > -0.01) currentSpeed = 0;
            }
            
            if (Math.abs(currentSpeed) > 0.05) {
                float effectiveTurnRadius = (float) Math.max(0.2, Math.abs(currentSpeed) * turnRadius);
                float turnRate = turnRadius / effectiveTurnRadius;
                
                if (sideInput > 0) {
                    hullYaw += turnRate; 
                } else if (sideInput < 0) {
                    hullYaw -= turnRate;
                }
            }
            
            float radYaw = (float) Math.toRadians(hullYaw);
            Vec3 forwardDir = new Vec3(-Math.sin(radYaw), 0, Math.cos(radYaw));
            
            double ySpeed = entity.getDeltaMovement().y;
            if (type == 0 && !entity.onGround()) {
                ySpeed -= 0.08; 
            } else if (type == 1) {
                ySpeed = 0; 
            }
            
            velocity = new Vec3(forwardDir.x * currentSpeed, ySpeed, forwardDir.z * currentSpeed);
            entity.setDeltaMovement(velocity);
            
            entity.setYRot(hullYaw);
            entity.yBodyRot = hullYaw;
            entity.yHeadRot = hullYaw;
            
        } else { // Plane or Heli
            float targetPitch = forwardInput * 30.0f;
            float targetRoll = sideInput * -30.0f; 
            
            pitch += (targetPitch - pitch) * 0.1f;
            roll += (targetRoll - roll) * 0.1f;
            
            if (sideInput > 0) hullYaw += turnRadius;
            if (sideInput < 0) hullYaw -= turnRadius;
            
            rotation.identity()
                    .rotateY((float) Math.toRadians(hullYaw))
                    .rotateX((float) Math.toRadians(pitch))
                    .rotateZ((float) Math.toRadians(roll));
            
            Vector3f forwardVec = new Vector3f(0, 0, 1).rotate(rotation);
            
            if (forwardInput > 0 && currentSpeed < maxSpeed) {
                currentSpeed += acceleration;
            } else if (forwardInput < 0) {
                currentSpeed -= braking;
                if (currentSpeed < 0) currentSpeed = 0;
            } else {
                currentSpeed *= 0.95;
            }
            
            velocity = new Vec3(forwardVec.x, forwardVec.y, forwardVec.z).scale(currentSpeed);
            
            if (type == 2) { 
                if (currentSpeed < maxSpeed * 0.3f) {
                    velocity = velocity.add(0, -0.05, 0); 
                }
            } else if (type == 3) { 
                if (forwardInput == 0 && currentSpeed < 0.1) {
                    velocity = new Vec3(0, 0, 0); 
                }
            }
            
            entity.setDeltaMovement(velocity);
            
            entity.setYRot(hullYaw);
            entity.yBodyRot = hullYaw;
            entity.yHeadRot = hullYaw;
            entity.setXRot(pitch);
        }
    }

    public Quaternionf getRotation() {
        return rotation;
    }
}
