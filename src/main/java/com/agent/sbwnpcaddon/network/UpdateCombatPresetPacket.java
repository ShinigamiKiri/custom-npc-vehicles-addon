package com.agent.sbwnpcaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateCombatPresetPacket {
    private final int entityId;
    private final int preset;

    public UpdateCombatPresetPacket(int entityId, int preset) {
        this.entityId = entityId;
        this.preset = preset;
    }

    public UpdateCombatPresetPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.preset = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(preset);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                Entity e = sender.level().getEntity(entityId);
                if (e instanceof Mob mob) {
                    mob.getPersistentData().putInt("SbwCombatPreset", preset);
                }
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
