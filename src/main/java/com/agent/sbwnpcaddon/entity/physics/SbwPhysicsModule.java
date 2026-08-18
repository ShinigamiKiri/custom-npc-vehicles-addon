package com.agent.sbwnpcaddon.entity.physics;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SbwPhysicsModule {
    private final Mob entity;
    
    // Rotation state
    private final Quaternionf rotation = new Quaternionf();
    private float hullYaw = 0.0f;
    private float turretYaw = 0.0f;
    private float pitch = 0.0f;
    private float roll = 0.0f;
    
    // Physics state
    private Vec3 velocity = Vec3.ZERO;
    private float acceleration = 0.02f;
    private float maxSpeed = 1.5f;
    private float turnSpeed = 2.0f;
    
    public SbwPhysicsModule(Mob entity) {
        this.entity = entity;
    }
    
    public void tickSteering(boolean forward, boolean backward, boolean left, boolean right) {
        // Smooth vector-based steering to eliminate yaw snapping
        if (left) {
            hullYaw -= turnSpeed;
        }
        if (right) {
            hullYaw += turnSpeed;
        }
        
        float radYaw = (float) Math.toRadians(hullYaw);
        Vec3 forwardDir = new Vec3(-Math.sin(radYaw), 0, Math.cos(radYaw));
        
        double speed = velocity.length();
        if (forward && speed < maxSpeed) {
            velocity = velocity.add(forwardDir.scale(acceleration));
        } else if (backward) {
            velocity = velocity.subtract(forwardDir.scale(acceleration));
        }
        
        // Friction / Drag
        velocity = velocity.scale(0.95);
        entity.setDeltaMovement(velocity);
        
        // Apply smooth yaw back to entity for Custom NPCs API compatibility
        entity.setYRot(hullYaw);
        entity.yBodyRot = hullYaw;
        entity.yHeadRot = turretYaw;
    }
    
    public void tickFlight(float targetPitch, float targetYaw, float targetRoll) {
        // 3-axis quaternion-based rotation system (pitch, yaw, roll)
        pitch += (targetPitch - pitch) * 0.1f;
        hullYaw += (targetYaw - hullYaw) * 0.1f;
        roll += (targetRoll - roll) * 0.1f;
        
        rotation.identity()
                .rotateY((float) Math.toRadians(hullYaw))
                .rotateX((float) Math.toRadians(pitch))
                .rotateZ((float) Math.toRadians(roll));
        
        // Update flight vector
        Vector3f forwardVec = new Vector3f(0, 0, 1).rotate(rotation);
        velocity = new Vec3(forwardVec.x, forwardVec.y, forwardVec.z).scale(maxSpeed);
        entity.setDeltaMovement(velocity);
    }
    
    public void updateTurret(float targetTurretYaw) {
        // Independent rotating turret logic separate from hull
        float diff = targetTurretYaw - turretYaw;
        while (diff < -180.0F) diff += 360.0F;
        while (diff >= 180.0F) diff -= 360.0F;
        
        turretYaw += diff * 0.15f; // Smooth turret tracking
        entity.yHeadRot = turretYaw;
    }

    public Quaternionf getRotation() {
        return rotation;
    }
}
