package com.agent.sbwnpcaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenCommandDeviceGuiPacket {
    private final List<Integer> entityIds;
    private final List<String> entityNames;
    private final List<Integer> presets;

    public OpenCommandDeviceGuiPacket(List<Integer> entityIds, List<String> entityNames, List<Integer> presets) {
        this.entityIds = entityIds;
        this.entityNames = entityNames;
        this.presets = presets;
    }

    public OpenCommandDeviceGuiPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.entityIds = new ArrayList<>();
        this.entityNames = new ArrayList<>();
        this.presets = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.entityIds.add(buf.readInt());
            this.entityNames.add(buf.readUtf(32767));
            this.presets.add(buf.readInt());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityIds.size());
        for (int i = 0; i < entityIds.size(); i++) {
            buf.writeInt(entityIds.get(i));
            buf.writeUtf(entityNames.get(i));
            buf.writeInt(presets.get(i));
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                com.agent.sbwnpcaddon.client.ClientHelper.openCommandDeviceScreen(entityIds, entityNames, presets);
            });
        });
        ctx.setPacketHandled(true);
        return true;
    }
}
