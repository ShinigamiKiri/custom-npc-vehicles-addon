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
    private final boolean cancel;
    private final int mode;
    private final double x1, y1, z1;
    private final double x2, y2, z2;

    public IssueCommandDevicePacket(List<Integer> entityIds, boolean cancel, int mode, double x1, double y1, double z1, double x2, double y2, double z2) {
        this.entityIds = entityIds;
        this.cancel = cancel;
        this.mode = mode;
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        this.x2 = x2; this.y2 = y2; this.z2 = z2;
    }

    public IssueCommandDevicePacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.entityIds = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.entityIds.add(buf.readInt());
        }
        this.cancel = buf.readBoolean();
        this.mode = buf.readInt();
        this.x1 = buf.readDouble(); this.y1 = buf.readDouble(); this.z1 = buf.readDouble();
        this.x2 = buf.readDouble(); this.y2 = buf.readDouble(); this.z2 = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityIds.size());
        for (int id : entityIds) {
            buf.writeInt(id);
        }
        buf.writeBoolean(cancel);
        buf.writeInt(mode);
        buf.writeDouble(x1); buf.writeDouble(y1); buf.writeDouble(z1);
        buf.writeDouble(x2); buf.writeDouble(y2); buf.writeDouble(z2);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                if (sender.getMainHandItem().getItem() instanceof com.agent.sbwnpcaddon.item.CommandDeviceItem ||
                    sender.getOffhandItem().getItem() instanceof com.agent.sbwnpcaddon.item.CommandDeviceItem) {
                    
                    for (int id : entityIds) {
                        Entity e = sender.level().getEntity(id);
                        if (e instanceof Mob mob) {
                            if (cancel) {
                                com.agent.sbwnpcaddon.entity.ai.CommandDeviceHelper.cancelCommand(mob);
                            } else {
                                com.agent.sbwnpcaddon.entity.ai.CommandDeviceHelper.applyCommand(mob, mode, x1, y1, z1, x2, y2, z2);
                            }
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
