package com.agent.sbwnpcaddon.network;

import com.agent.sbwnpcaddon.entity.physics.SbwPhysicsModule;
import com.agent.sbwnpcaddon.item.VehicleConfigTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveVehicleConfigPacket {
    private final int entityId;
    private final int type;
    private final float maxSpeed;
    private final float acceleration;
    private final float braking;
    private final float turnRadius;
    private final int aircraftMode;
    private final boolean physicsEnabled;
    private final float modelYawOffset;
    private final boolean applyToAllClones;

    public SaveVehicleConfigPacket(int entityId, int type, float maxSpeed, float acceleration, float braking, float turnRadius, int aircraftMode, boolean physicsEnabled, float modelYawOffset, boolean applyToAllClones) {
        this.entityId = entityId;
        this.type = type;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.braking = braking;
        this.turnRadius = turnRadius;
        this.aircraftMode = aircraftMode;
        this.physicsEnabled = physicsEnabled;
        this.modelYawOffset = modelYawOffset;
        this.applyToAllClones = applyToAllClones;
    }

    public SaveVehicleConfigPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.type = buf.readInt();
        this.maxSpeed = buf.readFloat();
        this.acceleration = buf.readFloat();
        this.braking = buf.readFloat();
        this.turnRadius = buf.readFloat();
        this.aircraftMode = buf.readInt();
        this.physicsEnabled = buf.readBoolean();
        this.modelYawOffset = buf.readFloat();
        this.applyToAllClones = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(type);
        buf.writeFloat(maxSpeed);
        buf.writeFloat(acceleration);
        buf.writeFloat(braking);
        buf.writeFloat(turnRadius);
        buf.writeInt(aircraftMode);
        buf.writeBoolean(physicsEnabled);
        buf.writeFloat(modelYawOffset);
        buf.writeBoolean(applyToAllClones);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {
                Entity targetEntity = ctx.getSender().level().getEntity(entityId);
                if (targetEntity instanceof Mob mob) {
                    applyToEntity(mob);

                    if (applyToAllClones) {
                        CompoundTag tag = new CompoundTag();
                        mob.saveWithoutId(tag);
                        if (tag.contains("ClonedName") && tag.contains("ClonedTab")) {
                            String cloneName = tag.getString("ClonedName");
                            int cloneTab = tag.getInt("ClonedTab");

                            for (ServerLevel level : targetEntity.getServer().getAllLevels()) {
                                for (Entity e : level.getAllEntities()) {
                                    if (e instanceof Mob otherMob && e != targetEntity) {
                                        CompoundTag eTag = new CompoundTag();
                                        e.saveWithoutId(eTag);
                                        if (eTag.contains("ClonedName") && eTag.contains("ClonedTab")) {
                                            if (eTag.getString("ClonedName").equals(cloneName) && eTag.getInt("ClonedTab") == cloneTab) {
                                                applyToEntity(otherMob);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        return true;
    }

    private void applyToEntity(Mob mob) {
        mob.getPersistentData().putInt("SbwVehicleType", type);
        mob.getPersistentData().putFloat("SbwMaxSpeed", maxSpeed);
        mob.getPersistentData().putFloat("SbwAcceleration", acceleration);
        mob.getPersistentData().putFloat("SbwBraking", braking);
        mob.getPersistentData().putFloat("SbwTurnRadius", turnRadius);
        mob.getPersistentData().putInt("SbwAircraftMode", aircraftMode);
        mob.getPersistentData().putBoolean("SbwPhysicsEnabled", physicsEnabled);
        mob.getPersistentData().putFloat("SbwModelYawOffset", modelYawOffset);

        if (physicsEnabled) {
            VehicleConfigTool.physicsModules.put(mob, new SbwPhysicsModule(mob));
        } else {
            VehicleConfigTool.physicsModules.remove(mob);
        }
    }
}
