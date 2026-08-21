package com.agent.sbwnpcaddon.network;

import com.agent.sbwnpcaddon.menu.NpcTradingMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TradeCompletePacket {
    public TradeCompletePacket() {}

    public TradeCompletePacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null && player.containerMenu instanceof NpcTradingMenu tradeMenu) {
                tradeMenu.tryCompleteTrade();
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
