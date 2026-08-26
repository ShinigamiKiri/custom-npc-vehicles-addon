package com.agent.sbwnpcaddon.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.function.Supplier;

public class UpdateActiveProjectilePacket {
    private final int entityId;
    private final int activeIndex;

    public UpdateActiveProjectilePacket(int entityId, int activeIndex) {
        this.entityId = entityId;
        this.activeIndex = activeIndex;
    }

    public UpdateActiveProjectilePacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.activeIndex = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(activeIndex);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {
                Entity targetEntity = ctx.getSender().level().getEntity(entityId);
                if (targetEntity instanceof Mob mob) {
                    mob.getPersistentData().putInt("SbwActiveProjectileIndex", activeIndex);
                    
                    if (mob instanceof EntityNPCInterface npc) {
                        ListTag loadout = mob.getPersistentData().getList("SbwProjectileLoadout", 10);
                        if (activeIndex >= 0 && activeIndex < loadout.size()) {
                            CompoundTag entry = loadout.getCompound(activeIndex);
                            if (entry.contains("DataRanged")) {
                                npc.stats.ranged.load(entry.getCompound("DataRanged"));
                            }
                            if (entry.contains("Item")) {
                                ItemStack item = ItemStack.of(entry.getCompound("Item"));
                                npc.inventory.setProjectile(noppes.npcs.api.NpcAPI.Instance().getIItemStack(item));
                            } else {
                                npc.inventory.setProjectile(noppes.npcs.api.NpcAPI.Instance().getIItemStack(ItemStack.EMPTY));
                            }
                            npc.updateAI = true;
                            npc.updateClient = true;
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}

