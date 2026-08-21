package com.agent.sbwnpcaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenCommandDeviceGuiPacket {
    private final List<Integer> entityIds;
    private final List<String> entityNames;

    public OpenCommandDeviceGuiPacket(List<Integer> entityIds, List<String> entityNames) {
        this.entityIds = entityIds;
        this.entityNames = entityNames;
    }

    public OpenCommandDeviceGuiPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.entityIds = new ArrayList<>();
        this.entityNames = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.entityIds.add(buf.readInt());
            this.entityNames.add(buf.readUtf(32767));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityIds.size());
        for (int i = 0; i < entityIds.size(); i++) {
            buf.writeInt(entityIds.get(i));
            buf.writeUtf(entityNames.get(i));
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                com.agent.sbwnpcaddon.client.ClientHelper.openCommandDeviceScreen(entityIds, entityNames);
            });
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
