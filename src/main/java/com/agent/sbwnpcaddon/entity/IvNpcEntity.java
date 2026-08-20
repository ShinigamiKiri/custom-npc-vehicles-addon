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
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SbwPhysicsEnabled")) {
            var data = this.getPersistentData();
            data.putInt("SbwVehicleType", tag.getInt("SbwVehicleType"));
            data.putFloat("SbwMaxSpeed", tag.getFloat("SbwMaxSpeed"));
            data.putFloat("SbwAcceleration", tag.getFloat("SbwAcceleration"));
            data.putFloat("SbwBraking", tag.getFloat("SbwBraking"));
            data.putFloat("SbwTurnRadius", tag.getFloat("SbwTurnRadius"));
            data.putBoolean("SbwPhysicsEnabled", tag.getBoolean("SbwPhysicsEnabled"));
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        if (this.getPersistentData().contains("SbwSeatOffset")) {
            return this.getPersistentData().getDouble("SbwSeatOffset");
        }
        return (double)this.getDimensions(this.getPose()).height * 0.45D;
    }
}
