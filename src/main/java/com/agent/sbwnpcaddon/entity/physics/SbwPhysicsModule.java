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

        // Neutralize native speed attribute to prevent Custom NPCs from moving the entity concurrently
        var moveAttr = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (moveAttr != null) {
            moveAttr.setBaseValue(0.0);
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
            
            if (forwardInput == 0 && sideInput == 0 && Math.abs(currentSpeed) < 0.01) {
                actualVelX = 0;
                actualVelZ = 0;
            }

            // 4. One single authoritative piece of code that writes rotation
            updateEntityRotation(hullYaw, pitch, roll);
            
        } else { // Aircraft (2=Plane, 3=Heli)
            
            boolean isHoverMode = (type == 3);
            if (entity.getPersistentData().contains("SbwAircraftMode")) {
                isHoverMode = (entity.getPersistentData().getInt("SbwAircraftMode") == 0);
            }
            
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
                // Aircraft loses speed over time if no throttle
                currentSpeed -= braking * 0.5;
                if (currentSpeed < 0) currentSpeed = 0;
            }
            
            float targetPitch = 0.0f;
            float targetRoll = 0.0f;
            float targetYawRate = 0.0f;

            if (isHoverMode) { // Hover/Stationary Mode (Helicopter/VTOL)
                // Helicopters can hover and turn freely
                float maxTurnRate = Math.min(turnRadius, 5.0f);
                targetYawRate = sideInput * maxTurnRate;
                
                targetPitch = forwardInput * 20.0f;
                targetRoll = sideInput * -20.0f;
            } else { // Runway Takeoff Mode (Plane)
                // Planes MUST have forward airspeed to turn effectively
                float maxTurnRate = Math.min(turnRadius, 3.0f);
                // Turn rate depends on speed and sideInput
                targetYawRate = sideInput * maxTurnRate * speedRatio;
                
                // Roll into the turn
                targetRoll = sideInput * -45.0f * speedRatio;
                
                // Add pitch based on wanting to climb/dive if guided by MoveControl
                if (entity.getMoveControl() instanceof VehicleMoveControl vmc) {
                     if (vmc.wantedY > entity.getY() + 1) {
                         targetPitch = -20.0f * speedRatio; // Pitch up
                     } else if (vmc.wantedY < entity.getY() - 1) {
                         targetPitch = 20.0f * speedRatio; // Pitch down
                     }
                }
                // If we are above takeoff speed but on the ground and want to go up, force pitch up
                float stallSpeedSq = maxSpeed * maxSpeed * 0.3f;
                if (currentSpeed * currentSpeed >= stallSpeedSq && entity.onGround() && entity.getMoveControl() instanceof VehicleMoveControl vmc && vmc.wantedY > entity.getY()) {
                     targetPitch = -20.0f; 
                }
            }
            
            // Smoothly update pitch, roll, yaw
            pitch += (targetPitch - pitch) * 0.1f;
            roll += (targetRoll - roll) * 0.1f;
            
            actualTurnRate += (targetYawRate - actualTurnRate) * 0.2f;
            hullYaw += actualTurnRate;

            updateEntityRotation(hullYaw, pitch, roll);

            // Vector forward based on rotation using standard Minecraft math
            float radYaw = (float) Math.toRadians(hullYaw);
            float radPitch = (float) Math.toRadians(pitch);
            double fX = -Math.sin(radYaw) * Math.cos(radPitch);
            double fY = -Math.sin(radPitch);
            double fZ = Math.cos(radYaw) * Math.cos(radPitch);
            
            // The engine pushes in the forwardVec direction with currentSpeed
            double targetVelX = fX * currentSpeed;
            double targetVelY = fY * currentSpeed; // Engine contributing to Y velocity (climbing/diving)
            double targetVelZ = fZ * currentSpeed;
            
            // Aerodynamic momentum correction / slip factor
            float aeroBlend = isHoverMode ? 0.9f : 0.05f; // Helis are snappy, planes drift and smoothly blend
            actualVelX += (targetVelX - actualVelX) * aeroBlend;
            actualVelZ += (targetVelZ - actualVelZ) * aeroBlend;
            
            if (forwardInput == 0 && sideInput == 0 && Math.abs(currentSpeed) < 0.01) {
                actualVelX = 0;
                actualVelZ = 0;
            }

            // Lift mechanics
            float gravity = 0.08f;
            
            if (!isHoverMode) { // Plane Lift
                float stallSpeedSq = maxSpeed * maxSpeed * 0.3f; // Stall threshold (~55% max speed)
                float speedSquared = (float) (currentSpeed * currentSpeed);
                
                actualVelY -= gravity; // Always apply gravity first
                
                if (speedSquared < stallSpeedSq) {
                    // Below takeoff speed: no lift generated. Stay glued to ground/fall.
                    // Ignore upward pitch so we don't climb like a helicopter.
                    if (targetVelY > 0) targetVelY = 0;
                    
                    // Allow engine to push nose down if falling
                    actualVelY += (targetVelY - actualVelY) * aeroBlend;
                } else {
                    // Above takeoff speed: lift counters gravity perfectly (equilibrium)
                    actualVelY += gravity; 
                    
                    // Climb/dive is now dictated purely by engine pitch (targetVelY)
                    actualVelY += (targetVelY - actualVelY) * aeroBlend;
                    
                    // If pitch is forcing us up, ensure positive actualVelY
                    if (targetVelY > 0 && actualVelY < targetVelY) {
                        actualVelY += (targetVelY - actualVelY) * 0.1f;
                    }
                }
                
                // Apply vertical drag/damping so we don't infinitely accelerate upwards/downwards
                actualVelY *= 0.90;
                
            } else { // Helicopter Lift
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
            entity.setMaxUpStep(1.0f); // Allow planes/helis to taxi over small bumps without horizontal collision stopping them
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

        System.out.printf("DEBUG SbwPhysics: Entity=%s yRot=%.2f deltaX=%.4f deltaZ=%.4f currentSpeed=%.4f%n",
                          entity.getName().getString(), yaw, actualVelX, actualVelZ, currentSpeed);
    }

    public Quaternionf getRotation() {
        return rotation;
    }
}
