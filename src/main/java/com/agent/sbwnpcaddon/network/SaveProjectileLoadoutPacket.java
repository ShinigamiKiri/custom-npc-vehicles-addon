package com.agent.sbwnpcaddon.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveProjectileLoadoutPacket {
    private final int entityId;
    private final ListTag loadout;

    public SaveProjectileLoadoutPacket(int entityId, ListTag loadout) {
        this.entityId = entityId;
        this.loadout = loadout;
    }

    public SaveProjectileLoadoutPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        CompoundTag tag = buf.readNbt();
        this.loadout = tag != null ? tag.getList("Loadout", 10) : new ListTag();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        CompoundTag tag = new CompoundTag();
        tag.put("Loadout", loadout);
        buf.writeNbt(tag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {
                Entity targetEntity = ctx.getSender().level().getEntity(entityId);
                if (targetEntity instanceof Mob mob) {
                    mob.getPersistentData().put("SbwProjectileLoadout", loadout);
                    int active = mob.getPersistentData().getInt("SbwActiveProjectileIndex");
                    if (active >= loadout.size()) {
                        mob.getPersistentData().putInt("SbwActiveProjectileIndex", 0);
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}

