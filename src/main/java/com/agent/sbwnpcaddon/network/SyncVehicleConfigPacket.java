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
    private final boolean physicsEnabled;

    public SyncVehicleConfigPacket(int entityId, int type, float maxSpeed, float acceleration, float braking, float turnRadius, boolean physicsEnabled) {
        this.entityId = entityId;
        this.type = type;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.braking = braking;
        this.turnRadius = turnRadius;
        this.physicsEnabled = physicsEnabled;
    }

    public SyncVehicleConfigPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.type = buf.readInt();
        this.maxSpeed = buf.readFloat();
        this.acceleration = buf.readFloat();
        this.braking = buf.readFloat();
        this.turnRadius = buf.readFloat();
        this.physicsEnabled = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(type);
        buf.writeFloat(maxSpeed);
        buf.writeFloat(acceleration);
        buf.writeFloat(braking);
        buf.writeFloat(turnRadius);
        buf.writeBoolean(physicsEnabled);
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
                    living.getPersistentData().putBoolean("SbwPhysicsEnabled", physicsEnabled);
                    
                    net.minecraft.client.Minecraft.getInstance().setScreen(new com.agent.sbwnpcaddon.client.screen.VehicleConfigScreen(living));
                }
            });
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
