package com.agent.sbwnpcaddon.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl;
import com.agent.sbwnpcaddon.entity.physics.VehicleLookControl;

public class IvNpcEntity extends PathfinderMob {
    private final String modelName;

    public IvNpcEntity(EntityType<? extends PathfinderMob> entityType, Level level, String modelName) {
        super(entityType, level);
        this.modelName = modelName;
        // Permanently bind vehicle physics AI controls
        this.moveControl = new VehicleMoveControl(this);
        this.lookControl = new VehicleLookControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    public String getModelName() {
        return this.modelName;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        var data = this.getPersistentData();
        if (data.contains("SbwPhysicsEnabled")) {
            tag.putInt("SbwVehicleType", data.getInt("SbwVehicleType"));
            tag.putFloat("SbwMaxSpeed", data.getFloat("SbwMaxSpeed"));
            tag.putFloat("SbwAcceleration", data.getFloat("SbwAcceleration"));
            tag.putFloat("SbwBraking", data.getFloat("SbwBraking"));
            tag.putFloat("SbwTurnRadius", data.getFloat("SbwTurnRadius"));
            tag.putBoolean("SbwPhysicsEnabled", data.getBoolean("SbwPhysicsEnabled"));
        }
        if (data.contains("SbwAircraftMode")) tag.putInt("SbwAircraftMode", data.getInt("SbwAircraftMode"));
        if (data.contains("SbwCommandActive")) tag.putBoolean("SbwCommandActive", data.getBoolean("SbwCommandActive"));
        if (data.contains("SbwCommandMode")) tag.putInt("SbwCommandMode", data.getInt("SbwCommandMode"));
        if (data.contains("SbwPrioritizeSelfDefense")) tag.putBoolean("SbwPrioritizeSelfDefense", data.getBoolean("SbwPrioritizeSelfDefense"));
        if (data.contains("SbwForceOwnerAssist")) tag.putBoolean("SbwForceOwnerAssist", data.getBoolean("SbwForceOwnerAssist"));
        if (data.contains("SbwSeatOffset")) tag.putDouble("SbwSeatOffset", data.getDouble("SbwSeatOffset"));
        if (data.contains("SbwCommandPatrol")) tag.putBoolean("SbwCommandPatrol", data.getBoolean("SbwCommandPatrol"));
        if (data.contains("SbwCombatPreset")) tag.putInt("SbwCombatPreset", data.getInt("SbwCombatPreset"));
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        var data = this.getPersistentData();
        if (tag.contains("SbwPhysicsEnabled")) {
            data.putInt("SbwVehicleType", tag.getInt("SbwVehicleType"));
            data.putFloat("SbwMaxSpeed", tag.getFloat("SbwMaxSpeed"));
            data.putFloat("SbwAcceleration", tag.getFloat("SbwAcceleration"));
            data.putFloat("SbwBraking", tag.getFloat("SbwBraking"));
            data.putFloat("SbwTurnRadius", tag.getFloat("SbwTurnRadius"));
            data.putBoolean("SbwPhysicsEnabled", tag.getBoolean("SbwPhysicsEnabled"));
        }
        if (tag.contains("SbwAircraftMode")) data.putInt("SbwAircraftMode", tag.getInt("SbwAircraftMode"));
        if (tag.contains("SbwCommandActive")) data.putBoolean("SbwCommandActive", tag.getBoolean("SbwCommandActive"));
        if (tag.contains("SbwCommandMode")) data.putInt("SbwCommandMode", tag.getInt("SbwCommandMode"));
        if (tag.contains("SbwPrioritizeSelfDefense")) data.putBoolean("SbwPrioritizeSelfDefense", tag.getBoolean("SbwPrioritizeSelfDefense"));
        if (tag.contains("SbwForceOwnerAssist")) data.putBoolean("SbwForceOwnerAssist", tag.getBoolean("SbwForceOwnerAssist"));
        if (tag.contains("SbwSeatOffset")) data.putDouble("SbwSeatOffset", tag.getDouble("SbwSeatOffset"));
        if (tag.contains("SbwCommandPatrol")) data.putBoolean("SbwCommandPatrol", tag.getBoolean("SbwCommandPatrol"));
        if (tag.contains("SbwCombatPreset")) data.putInt("SbwCombatPreset", tag.getInt("SbwCombatPreset"));
    }

    @Override
    public double getPassengersRidingOffset() {
        if (this.getPersistentData().contains("SbwSeatOffset")) {
            return this.getPersistentData().getDouble("SbwSeatOffset");
        }
        return (double)this.getDimensions(this.getPose()).height * 0.45D;
    }
}
