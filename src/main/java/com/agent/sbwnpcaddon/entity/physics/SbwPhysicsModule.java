package com.agent.sbwnpcaddon.entity.physics;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.util.Mth;

public class SbwPhysicsModule {
    // --- New constants for mechanical feel ---
    private static final float INERTIA_BLEND_FACTOR = 0.05f; 
    private static final float STEERING_RAMP_SPEED = 0.05f;  
    private static final float ACCEL_CURVE_EXPONENT = 2.5f;  
    private static final int BRAKE_DELAY_TICKS = 15;          
    // -----------------------------------------

    private final Mob entity;
    
    private final Quaternionf rotation = new Quaternionf();
    private float hullYaw = 0.0f;
    private float pitch = 0.0f;
    private float roll = 0.0f;
    
    private double currentSpeed = 0.0;
    private Vec3 velocity = Vec3.ZERO;
    
    // State variables for mechanical smoothing
    private float actualTurnRate = 0.0f;
    private int brakeTicks = 0;
    private double actualVelX = 0.0;
    private double actualVelY = 0.0;
    private double actualVelZ = 0.0;
    
    public SbwPhysicsModule(Mob entity) {
        this.entity = entity;
        this.hullYaw = entity.getYRot();
        this.actualVelX = entity.getDeltaMovement().x;
        this.actualVelY = entity.getDeltaMovement().y;
        this.actualVelZ = entity.getDeltaMovement().z;
    }
    
    public void tick() {
        int type = entity.getPersistentData().getInt("SbwVehicleType");
        
        // Scale down speeds significantly so 0.5 is slower than a mob
        float speedScale = 0.25f;
        float maxSpeed = (entity.getPersistentData().contains("SbwMaxSpeed") ? entity.getPersistentData().getFloat("SbwMaxSpeed") : 0.5f) * speedScale;
        float acceleration = (entity.getPersistentData().contains("SbwAcceleration") ? entity.getPersistentData().getFloat("SbwAcceleration") : 0.005f) * speedScale;
        float braking = (entity.getPersistentData().contains("SbwBraking") ? entity.getPersistentData().getFloat("SbwBraking") : 0.02f) * speedScale;
        float turnRadius = entity.getPersistentData().contains("SbwTurnRadius") ? entity.getPersistentData().getFloat("SbwTurnRadius") : 1.0f;
        
        float forwardInput = entity.zza; 
        float sideInput = entity.xxa; 
        
        if (entity.getMoveControl() instanceof VehicleMoveControl vmc) {
            forwardInput = vmc.forwardIntent;
            sideInput = vmc.sideIntent;
        }

        float speedRatio = (float) (Math.abs(currentSpeed) / maxSpeed);
        speedRatio = Mth.clamp(speedRatio, 0.0f, 1.0f);

        if (type == 0 || type == 1) { // Ground (0) or Boat (1)
            // Ground vehicle mechanics
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
                if (Math.abs(currentSpeed) < 0.01) currentSpeed = 0;
            }
            
            float targetTurnRate = 0.0f;
            // 1. Rotation never happens while speed is near zero
            if (Math.abs(currentSpeed) > 0.01) {
                float maxTurnRate = Math.min(turnRadius, 5.0f);
                // 2. Turn rate decreases as speed increases
                float baseTurnRate = maxTurnRate / (1.0f + speedRatio * 5.0f);
                
                // Allow reverse steering inversion
                float steerDirection = (currentSpeed < 0) ? -sideInput : sideInput;
                targetTurnRate = steerDirection * baseTurnRate;
            }
            
            actualTurnRate += (targetTurnRate - actualTurnRate) * STEERING_RAMP_SPEED;
            hullYaw += actualTurnRate;
            
            // 3. Turning bleeds forward speed (Drag/scrubbing)
            currentSpeed *= (1.0 - Math.abs(actualTurnRate) * 0.015);
            
            float radYaw = (float) Math.toRadians(hullYaw);
            double targetVelX = -Math.sin(radYaw) * currentSpeed;
            double targetVelZ = Math.cos(radYaw) * currentSpeed;
            
            actualVelX += (targetVelX - actualVelX) * INERTIA_BLEND_FACTOR;
            actualVelZ += (targetVelZ - actualVelZ) * INERTIA_BLEND_FACTOR;
            
            if (type == 0 && !entity.onGround()) {
                actualVelY -= 0.08; 
            } else if (type == 0 && entity.onGround()) {
                actualVelY = 0;
            } else if (type == 1) {
                actualVelY = 0; 
            }
            
            velocity = new Vec3(actualVelX, actualVelY, actualVelZ);
            if (type == 0 || type == 1) {
                entity.setMaxUpStep(1.0f);
            }
            entity.move(net.minecraft.world.entity.MoverType.SELF, velocity);
            
            Vec3 postMove = entity.getDeltaMovement();
            actualVelX = postMove.x;
            actualVelY = postMove.y;
            actualVelZ = postMove.z;
            
            if (entity.horizontalCollision) {
                currentSpeed *= 0.1;
            }
            
            entity.setDeltaMovement(Vec3.ZERO);
            
            float targetRoll = actualTurnRate * speedRatio * -15.0f; 
            float targetPitch = 0.0f;
            if (forwardInput > 0) targetPitch = -2.0f * speedRatio;
            else if (forwardInput < 0) targetPitch = 4.0f * Math.min(1.0f, (float)brakeTicks / BRAKE_DELAY_TICKS);
            
            pitch += (targetPitch - pitch) * 0.15f;
            roll += (targetRoll - roll) * 0.15f;
            
            // 4. One single authoritative piece of code that writes rotation
            updateEntityRotation(hullYaw, pitch, roll);
            
        } else { // Plane (2) or Heli (3)
            
            // Throttle controls forward target speed
            if (forwardInput > 0) {
                brakeTicks = 0;
                currentSpeed += acceleration;
                if (currentSpeed > maxSpeed) currentSpeed = maxSpeed;
            } else if (forwardInput < 0) {
                brakeTicks++;
                float brakeMultiplier = Math.min(1.0f, (float) brakeTicks / BRAKE_DELAY_TICKS);
                currentSpeed -= braking * brakeMultiplier;
                if (currentSpeed < 0) currentSpeed = 0;
            } else {
                brakeTicks = 0;
                // Plane loses speed over time if no throttle
                currentSpeed -= braking * 0.5;
                if (currentSpeed < 0) currentSpeed = 0;
            }
            
            float targetPitch = 0.0f;
            float targetRoll = 0.0f;
            float targetYawRate = 0.0f;

            if (type == 3) { // Helicopter
                // Helicopters can hover and turn freely
                float maxTurnRate = Math.min(turnRadius, 5.0f);
                targetYawRate = sideInput * maxTurnRate;
                
                targetPitch = forwardInput * 20.0f;
                targetRoll = sideInput * -20.0f;
            } else { // Plane
                // Planes MUST have forward airspeed to turn effectively
                float maxTurnRate = Math.min(turnRadius, 3.0f);
                // Turn rate depends on speed and sideInput
                targetYawRate = sideInput * maxTurnRate * speedRatio;
                
                // Roll into the turn
                targetRoll = sideInput * -45.0f * speedRatio;
                
                // Add pitch based on wanting to climb/dive if guided by MoveControl
                if (entity.getMoveControl() instanceof VehicleMoveControl vmc) {
                     if (vmc.wantedY > entity.getY() + 1) {
                         targetPitch = -20.0f * speedRatio;
                     } else if (vmc.wantedY < entity.getY() - 1) {
                         targetPitch = 20.0f * speedRatio;
                     }
                }
            }
            
            // Smoothly update pitch, roll, yaw
            pitch += (targetPitch - pitch) * 0.1f;
            roll += (targetRoll - roll) * 0.1f;
            
            actualTurnRate += (targetYawRate - actualTurnRate) * 0.2f;
            hullYaw += actualTurnRate;

            updateEntityRotation(hullYaw, pitch, roll);

            // Vector forward based on rotation
            Vector3f forwardVec = new Vector3f(0, 0, 1).rotate(this.rotation);
            
            // The engine pushes in the forwardVec direction with currentSpeed
            double targetVelX = forwardVec.x * currentSpeed;
            double targetVelY = forwardVec.y * currentSpeed; // Engine contributing to Y velocity (climbing/diving)
            double targetVelZ = forwardVec.z * currentSpeed;
            
            // Aerodynamic momentum correction / slip factor
            float aeroBlend = (type == 3) ? 0.9f : 0.05f; // Helis are snappy, planes drift and smoothly blend
            actualVelX += (targetVelX - actualVelX) * aeroBlend;
            actualVelZ += (targetVelZ - actualVelZ) * aeroBlend;
            
            // Lift mechanics
            float gravity = 0.08f;
            
            if (type == 2) { // Plane Lift
                // Lift = v^2 * liftConstant
                float speedSquared = (float) (currentSpeed * currentSpeed);
                float liftConstant = gravity / (maxSpeed * maxSpeed * 0.3f); // Stall below ~55% max speed
                float lift = speedSquared * liftConstant;
                
                // Cap lift to avoid launching into space infinitely when going fast, just roughly counter gravity
                if (lift > gravity * 1.5f) lift = gravity * 1.5f;
                
                actualVelY -= gravity; // Apply gravity
                actualVelY += lift;    // Apply lift
                
                // Blend engine climb/dive velocity based on forward pitch
                actualVelY += (targetVelY - actualVelY) * aeroBlend;
                
            } else if (type == 3) { // Helicopter Lift
                // Helicopters use engine thrust directly as lift
                if (entity.getMoveControl() instanceof VehicleMoveControl vmc) {
                    if (vmc.wantedY > entity.getY() + 0.5) {
                        actualVelY += 0.02;
                    } else if (vmc.wantedY < entity.getY() - 0.5) {
                        actualVelY -= 0.02;
                    } else {
                        actualVelY *= 0.8; // dampen to hover
                    }
                } else {
                    if (forwardInput == 0) {
                        actualVelY *= 0.8;
                    }
                }
            }
            
            velocity = new Vec3(actualVelX, actualVelY, actualVelZ);
            entity.move(net.minecraft.world.entity.MoverType.SELF, velocity);
            
            Vec3 postMove = entity.getDeltaMovement();
            actualVelX = postMove.x;
            actualVelY = postMove.y;
            actualVelZ = postMove.z;
            
            if (entity.horizontalCollision) {
                currentSpeed *= 0.1;
            }
            
            entity.setDeltaMovement(Vec3.ZERO);
        }
    }
    
    private void updateEntityRotation(float yaw, float pitch, float roll) {
        this.rotation.identity()
            .rotateY((float) Math.toRadians(yaw))
            .rotateX((float) Math.toRadians(pitch))
            .rotateZ((float) Math.toRadians(roll));
            
        entity.setYRot(yaw);
        entity.yBodyRot = yaw;
        entity.yHeadRot = yaw;
        entity.setXRot(pitch);
    }

    public Quaternionf getRotation() {
        return rotation;
    }
}
