package com.agent.sbwnpcaddon.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class SbwNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("sbw_npc_addon", "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SaveVehicleConfigPacket.class, SaveVehicleConfigPacket::toBytes, SaveVehicleConfigPacket::new, SaveVehicleConfigPacket::handle);
        CHANNEL.registerMessage(id++, SyncVehicleConfigPacket.class, SyncVehicleConfigPacket::toBytes, SyncVehicleConfigPacket::new, SyncVehicleConfigPacket::handle);
        CHANNEL.registerMessage(id++, OpenCommandDeviceGuiPacket.class, OpenCommandDeviceGuiPacket::toBytes, OpenCommandDeviceGuiPacket::new, OpenCommandDeviceGuiPacket::handle);
        CHANNEL.registerMessage(id++, IssueCommandDevicePacket.class, IssueCommandDevicePacket::toBytes, IssueCommandDevicePacket::new, IssueCommandDevicePacket::handle);
        CHANNEL.registerMessage(id++, TradeCompletePacket.class, TradeCompletePacket::toBytes, TradeCompletePacket::new, TradeCompletePacket::handle);
        CHANNEL.registerMessage(id++, UpdateCombatPresetPacket.class, UpdateCombatPresetPacket::toBytes, UpdateCombatPresetPacket::new, UpdateCombatPresetPacket::handle);
        CHANNEL.registerMessage(id++, SaveProjectileLoadoutPacket.class, SaveProjectileLoadoutPacket::toBytes, SaveProjectileLoadoutPacket::new, SaveProjectileLoadoutPacket::handle);
        CHANNEL.registerMessage(id++, UpdateActiveProjectilePacket.class, UpdateActiveProjectilePacket::toBytes, UpdateActiveProjectilePacket::new, UpdateActiveProjectilePacket::handle);
    }
}
