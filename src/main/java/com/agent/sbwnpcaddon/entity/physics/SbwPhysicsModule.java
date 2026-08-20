package com.agent.sbwnpcaddon.entity.physics;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SbwPhysicsModule {
    // --- New constants for mechanical feel ---
    private static final float INERTIA_BLEND_FACTOR = 0.15f; // (1) Momentum/inertia blend: lower = heavier sliding
    private static final float STEERING_RAMP_SPEED = 0.15f;  // (2) Steering linkage lag: lower = slower steering response
    private static final float ACCEL_CURVE_EXPONENT = 1.5f;  // (3) Slower approach to max speed (higher = smoother top-out)
    private static final int BRAKE_DELAY_TICKS = 8;          // (3) Ticks before full brake force applies (soft braking)
    // -----------------------------------------

    private final Mob entity;
    
    private final Quaternionf rotation = new Quaternionf();
    private float hullYaw = 0.0f;
    private float pitch = 0.0f;
    private float roll = 0.0f;
    
    private double currentSpeed = 0.0;
    private Vec3 velocity = Vec3.ZERO;
    
    // New state variables for mechanical smoothing
    private float actualTurnRate = 0.0f;
    private int brakeTicks = 0;
    private double actualVelX = 0.0;
    private double actualVelZ = 0.0;
    
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
            float speedRatio = (float) (Math.abs(currentSpeed) / maxSpeed);
            speedRatio = Math.min(1.0f, Math.max(0.0f, speedRatio));

            if (forwardInput > 0) {
                brakeTicks = 0;
                if (currentSpeed < maxSpeed) {
                    float startFactor = Math.min(1.0f, 0.3f + speedRatio * 3.0f);
                    float curveFactor = (float) Math.pow(1.0f - speedRatio, ACCEL_CURVE_EXPONENT);
                    float accelMultiplier = startFactor * curveFactor;
                    currentSpeed += acceleration * accelMultiplier;
                }
            } else if (forwardInput < 0) {
                brakeTicks++;
                float brakeMultiplier = Math.min(1.0f, (float) brakeTicks / BRAKE_DELAY_TICKS);
                currentSpeed -= braking * brakeMultiplier;
                if (currentSpeed < -maxSpeed / 2) currentSpeed = -maxSpeed / 2;
            } else {
                brakeTicks = 0;
                currentSpeed *= 0.90; 
                if (currentSpeed < 0.01 && currentSpeed > -0.01) currentSpeed = 0;
            }
            
            float targetTurnRate = 0.0f;
            if (Math.abs(currentSpeed) > 0.01) {
                float maxTurnRate = Math.min(turnRadius, 5.0f);
                float baseTurnRate = maxTurnRate / (1.0f + speedRatio * 5.0f);
                
                if (sideInput > 0) {
                    targetTurnRate = baseTurnRate;
                } else if (sideInput < 0) {
                    targetTurnRate = -baseTurnRate;
                }
            }
            
            actualTurnRate += (targetTurnRate - actualTurnRate) * STEERING_RAMP_SPEED;
            hullYaw += actualTurnRate;
            
            // --- ADOPTED: Speed Bleed / Turning Drag (Flan's Mod) ---
            // Sharp turning bleeds forward momentum, forcing deceleration through tight corners.
            currentSpeed *= (1.0 - Math.abs(actualTurnRate) * 0.015);
            
            float radYaw = (float) Math.toRadians(hullYaw);
            double targetVelX = -Math.sin(radYaw) * currentSpeed;
            double targetVelZ = Math.cos(radYaw) * currentSpeed;
            
            actualVelX += (targetVelX - actualVelX) * INERTIA_BLEND_FACTOR;
            actualVelZ += (targetVelZ - actualVelZ) * INERTIA_BLEND_FACTOR;
            
            double ySpeed = entity.getDeltaMovement().y;
            if (type == 0 && !entity.onGround()) {
                ySpeed -= 0.08; 
            } else if (type == 1) {
                ySpeed = 0; 
            }
            
            velocity = new Vec3(actualVelX, ySpeed, actualVelZ);
            entity.setDeltaMovement(velocity);
            
            float targetRoll = actualTurnRate * speedRatio * -15.0f; 
            float targetPitch = 0.0f;
            if (forwardInput > 0) targetPitch = -2.0f * speedRatio;
            else if (forwardInput < 0) targetPitch = 4.0f * Math.min(1.0f, (float)brakeTicks / BRAKE_DELAY_TICKS);
            
            pitch += (targetPitch - pitch) * 0.15f;
            roll += (targetRoll - roll) * 0.15f;
            
            rotation.identity()
                    .rotateY((float) Math.toRadians(hullYaw))
                    .rotateX((float) Math.toRadians(pitch))
                    .rotateZ((float) Math.toRadians(roll));
            
            entity.setYRot(hullYaw);
            entity.yBodyRot = hullYaw;
            entity.yHeadRot = hullYaw;
            entity.setXRot(pitch);
            
        } else { // Plane or Heli
            float targetPitch = forwardInput * 30.0f;
            float targetRoll = sideInput * -30.0f; 
            
            pitch += (targetPitch - pitch) * 0.1f;
            roll += (targetRoll - roll) * 0.1f;
            
            float speedFactor = (float) Math.abs(currentSpeed);
            // Heli (type 3) can turn while hovering. Planes (type 2) must move.
            if (type == 3 || speedFactor > 0.01) {
                float maxTurnRate = Math.min(turnRadius, 5.0f);
                float turnRate = (type == 3) ? maxTurnRate : (maxTurnRate / (1.0f + speedFactor * 5.0f));
                
                if (sideInput > 0) hullYaw += turnRate;
                if (sideInput < 0) hullYaw -= turnRate;
            }
            
            rotation.identity()
                    .rotateY((float) Math.toRadians(hullYaw))
                    .rotateX((float) Math.toRadians(pitch))
                    .rotateZ((float) Math.toRadians(roll));
            
            Vector3f forwardVec = new Vector3f(0, 0, 1).rotate(rotation);
            
            if (forwardInput > 0 && currentSpeed < maxSpeed) {
                currentSpeed += acceleration;
                brakeTicks = 0;
            } else if (forwardInput < 0) {
                brakeTicks++;
                float brakeMultiplier = Math.min(1.0f, (float) brakeTicks / BRAKE_DELAY_TICKS);
                currentSpeed -= braking * brakeMultiplier;
                if (currentSpeed < 0) currentSpeed = 0;
            } else {
                currentSpeed *= 0.95;
                brakeTicks = 0;
            }
            
            // --- ADOPTED: Aerodynamic Momentum Correction (Planes) ---
            // Blend velocity towards forward vector, rather than instantly snapping
            double targetVelX = forwardVec.x * currentSpeed;
            double targetVelY = forwardVec.y * currentSpeed;
            double targetVelZ = forwardVec.z * currentSpeed;
            
            float aeroBlend = (type == 3) ? 1.0f : 0.05f; // Helicopters are snappy; planes drift
            actualVelX += (targetVelX - actualVelX) * aeroBlend;
            actualVelZ += (targetVelZ - actualVelZ) * aeroBlend;
            
            double actualVelY = entity.getDeltaMovement().y;
            actualVelY += (targetVelY - actualVelY) * aeroBlend;
            
            // --- ADOPTED: Speed-Squared Lift (Planes) ---
            if (type == 2) { 
                float speedSquared = (float) (currentSpeed * currentSpeed);
                // The lift force is proportional to v^2, acting counter to gravity
                float liftConstant = 2.0f; // Tuned for standard max speed ~0.5
                float lift = speedSquared * liftConstant;
                float gravity = 0.08f;
                
                // Cap lift to prevent floating off indefinitely, just offset gravity for level flight
                if (lift > gravity) {
                    lift = gravity;
                }
                actualVelY -= gravity;
                actualVelY += lift;
            } else if (type == 3) { 
                if (forwardInput == 0 && currentSpeed < 0.1) {
                    actualVelY *= 0.8;
                }
            }
            
            velocity = new Vec3(actualVelX, actualVelY, actualVelZ);
            entity.setDeltaMovement(velocity);
            
            actualVelX = velocity.x;
            actualVelZ = velocity.z;
            
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
