package com.agent.sbwnpcaddon.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityDimensions;

public class IvNpcEntity extends PathfinderMob {
    private final String modelName;

    public IvNpcEntity(EntityType<? extends PathfinderMob> entityType, Level level, String modelName) {
        super(entityType, level);
        this.modelName = modelName;
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
    public double getPassengersRidingOffset() {
        if (this.getPersistentData().contains("SbwSeatOffset")) {
            return this.getPersistentData().getDouble("SbwSeatOffset");
        }
        // For vehicles (especially jets/tanks), 0.4x to 0.5x height usually puts the player in the cockpit/seat
        // rather than floating on the roof (0.75x). 
        return (double)this.getDimensions(this.getPose()).height * 0.45D;
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions) {
        // Eye height should generally follow the upper hull
        return dimensions.height * 0.85F;
    }
}
