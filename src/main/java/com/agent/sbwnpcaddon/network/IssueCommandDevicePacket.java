package com.agent.sbwnpcaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class IssueCommandDevicePacket {
    private final List<Integer> entityIds;
    private final boolean patrolMode;
    private final double x1, y1, z1;
    private final double x2, y2, z2;

    public IssueCommandDevicePacket(List<Integer> entityIds, boolean patrolMode, double x1, double y1, double z1, double x2, double y2, double z2) {
        this.entityIds = entityIds;
        this.patrolMode = patrolMode;
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        this.x2 = x2; this.y2 = y2; this.z2 = z2;
    }

    public IssueCommandDevicePacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.entityIds = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.entityIds.add(buf.readInt());
        }
        this.patrolMode = buf.readBoolean();
        this.x1 = buf.readDouble(); this.y1 = buf.readDouble(); this.z1 = buf.readDouble();
        this.x2 = buf.readDouble(); this.y2 = buf.readDouble(); this.z2 = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityIds.size());
        for (int id : entityIds) {
            buf.writeInt(id);
        }
        buf.writeBoolean(patrolMode);
        buf.writeDouble(x1); buf.writeDouble(y1); buf.writeDouble(z1);
        buf.writeDouble(x2); buf.writeDouble(y2); buf.writeDouble(z2);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                // Ensure the player is holding the Command Device
                if (sender.getMainHandItem().getItem() instanceof com.agent.sbwnpcaddon.item.CommandDeviceItem ||
                    sender.getOffhandItem().getItem() instanceof com.agent.sbwnpcaddon.item.CommandDeviceItem) {
                    
                    for (int id : entityIds) {
                        Entity e = sender.level().getEntity(id);
                        if (e instanceof Mob mob) {
                            com.agent.sbwnpcaddon.entity.ai.CommandDeviceHelper.applyCommand(mob, patrolMode, x1, y1, z1, x2, y2, z2);
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
