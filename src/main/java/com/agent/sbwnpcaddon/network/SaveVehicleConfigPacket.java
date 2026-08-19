package com.agent.sbwnpcaddon.network;

import com.agent.sbwnpcaddon.entity.physics.SbwPhysicsModule;
import com.agent.sbwnpcaddon.item.VehicleConfigTool;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveVehicleConfigPacket {
    private final int entityId;
    private final int type; // 0=Ground, 1=Boat, 2=Plane, 3=Heli
    private final float maxSpeed;
    private final float acceleration;
    private final float braking;
    private final float turnRadius;
    private final boolean physicsEnabled;

    public SaveVehicleConfigPacket(int entityId, int type, float maxSpeed, float acceleration, float braking, float turnRadius, boolean physicsEnabled) {
        this.entityId = entityId;
        this.type = type;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.braking = braking;
        this.turnRadius = turnRadius;
        this.physicsEnabled = physicsEnabled;
    }

    public SaveVehicleConfigPacket(FriendlyByteBuf buf) {
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
            if (ctx.getSender() != null) {
                Entity entity = ctx.getSender().level().getEntity(entityId);
                if (entity instanceof Mob mob) {
                    mob.getPersistentData().putInt("SbwVehicleType", type);
                    mob.getPersistentData().putFloat("SbwMaxSpeed", maxSpeed);
                    mob.getPersistentData().putFloat("SbwAcceleration", acceleration);
                    mob.getPersistentData().putFloat("SbwBraking", braking);
                    mob.getPersistentData().putFloat("SbwTurnRadius", turnRadius);
                    mob.getPersistentData().putBoolean("SbwPhysicsEnabled", physicsEnabled);
                    
                    if (physicsEnabled) {
                        VehicleConfigTool.physicsModules.put(mob, new SbwPhysicsModule(mob));
                    } else {
                        VehicleConfigTool.physicsModules.remove(mob);
                    }
                }
            }
        });
        return true;
    }
}
