package com.agent.sbwnpcaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Supplier;

public class SyncVehicleConfigPacket {
    private final int entityId;
    private final int type;
    private final float maxSpeed;
    private final float acceleration;
    private final float braking;
    private final float turnRadius;
    private final int aircraftMode;
    private final boolean physicsEnabled;
    private final float modelYawOffset;

    public SyncVehicleConfigPacket(int entityId, int type, float maxSpeed, float acceleration, float braking, float turnRadius, int aircraftMode, boolean physicsEnabled, float modelYawOffset) {
        this.entityId = entityId;
        this.type = type;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.braking = braking;
        this.turnRadius = turnRadius;
        this.aircraftMode = aircraftMode;
        this.physicsEnabled = physicsEnabled;
        this.modelYawOffset = modelYawOffset;
    }

    public SyncVehicleConfigPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.type = buf.readInt();
        this.maxSpeed = buf.readFloat();
        this.acceleration = buf.readFloat();
        this.braking = buf.readFloat();
        this.turnRadius = buf.readFloat();
        this.aircraftMode = buf.readInt();
        this.physicsEnabled = buf.readBoolean();
        this.modelYawOffset = buf.readFloat();
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
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Entity entity = net.minecraft.client.Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof LivingEntity living) {
                    // Temporarily set client-side persistent data so the GUI can read it
                    living.getPersistentData().putInt("SbwVehicleType", type);
                    living.getPersistentData().putFloat("SbwMaxSpeed", maxSpeed);
                    living.getPersistentData().putFloat("SbwAcceleration", acceleration);
                    living.getPersistentData().putFloat("SbwBraking", braking);
                    living.getPersistentData().putFloat("SbwTurnRadius", turnRadius);
                    living.getPersistentData().putInt("SbwAircraftMode", aircraftMode);
                    living.getPersistentData().putBoolean("SbwPhysicsEnabled", physicsEnabled);
                    living.getPersistentData().putFloat("SbwModelYawOffset", modelYawOffset);
                    
                    net.minecraft.client.Minecraft.getInstance().setScreen(new com.agent.sbwnpcaddon.client.screen.VehicleConfigScreen(living));
                }
            });
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
