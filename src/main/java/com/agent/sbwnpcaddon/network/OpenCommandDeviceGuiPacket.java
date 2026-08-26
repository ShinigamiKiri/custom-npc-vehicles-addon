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
    private final List<Boolean> isCommandActive;
    private final List<Integer> activeModes;
    private final List<Double> targetXs, targetYs, targetZs;
    private final List<Double> targetX2s, targetY2s, targetZ2s;
    
    private final List<String> projectileLoadoutNames;
    private final List<Integer> activeProjectileIndices;

    public OpenCommandDeviceGuiPacket(List<Integer> entityIds, List<String> entityNames, List<Integer> presets,
                                      List<Boolean> isCommandActive, List<Integer> activeModes,
                                      List<Double> targetXs, List<Double> targetYs, List<Double> targetZs,
                                      List<Double> targetX2s, List<Double> targetY2s, List<Double> targetZ2s,
                                      List<String> projectileLoadoutNames, List<Integer> activeProjectileIndices) {
        this.entityIds = entityIds;
        this.entityNames = entityNames;
        this.presets = presets;
        this.isCommandActive = isCommandActive;
        this.activeModes = activeModes;
        this.targetXs = targetXs; this.targetYs = targetYs; this.targetZs = targetZs;
        this.targetX2s = targetX2s; this.targetY2s = targetY2s; this.targetZ2s = targetZ2s;
        this.projectileLoadoutNames = projectileLoadoutNames;
        this.activeProjectileIndices = activeProjectileIndices;
    }

    public OpenCommandDeviceGuiPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.entityIds = new ArrayList<>();
        this.entityNames = new ArrayList<>();
        this.presets = new ArrayList<>();
        this.isCommandActive = new ArrayList<>();
        this.activeModes = new ArrayList<>();
        this.targetXs = new ArrayList<>(); this.targetYs = new ArrayList<>(); this.targetZs = new ArrayList<>();
        this.targetX2s = new ArrayList<>(); this.targetY2s = new ArrayList<>(); this.targetZ2s = new ArrayList<>();
        this.projectileLoadoutNames = new ArrayList<>();
        this.activeProjectileIndices = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            this.entityIds.add(buf.readInt());
            this.entityNames.add(buf.readUtf(32767));
            this.presets.add(buf.readInt());
            this.isCommandActive.add(buf.readBoolean());
            this.activeModes.add(buf.readInt());
            this.targetXs.add(buf.readDouble()); this.targetYs.add(buf.readDouble()); this.targetZs.add(buf.readDouble());
            this.targetX2s.add(buf.readDouble()); this.targetY2s.add(buf.readDouble()); this.targetZ2s.add(buf.readDouble());
            this.projectileLoadoutNames.add(buf.readUtf(32767));
            this.activeProjectileIndices.add(buf.readInt());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityIds.size());
        for (int i = 0; i < entityIds.size(); i++) {
            buf.writeInt(entityIds.get(i));
            buf.writeUtf(entityNames.get(i));
            buf.writeInt(presets.get(i));
            buf.writeBoolean(isCommandActive.get(i));
            buf.writeInt(activeModes.get(i));
            buf.writeDouble(targetXs.get(i)); buf.writeDouble(targetYs.get(i)); buf.writeDouble(targetZs.get(i));
            buf.writeDouble(targetX2s.get(i)); buf.writeDouble(targetY2s.get(i)); buf.writeDouble(targetZ2s.get(i));
            buf.writeUtf(projectileLoadoutNames.get(i));
            buf.writeInt(activeProjectileIndices.get(i));
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                com.agent.sbwnpcaddon.client.ClientHelper.openCommandDeviceScreen(entityIds, entityNames, presets, isCommandActive, activeModes, targetXs, targetYs, targetZs, targetX2s, targetY2s, targetZ2s, projectileLoadoutNames, activeProjectileIndices);
            });
        });
        ctx.setPacketHandled(true);
        return true;
    }
}

