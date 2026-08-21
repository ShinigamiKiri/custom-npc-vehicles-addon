import os

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

write_file("src/main/java/com/agent/sbwnpcaddon/client/screen/NpcTradingSetupScreen.java", """package com.agent.sbwnpcaddon.client.screen;

import com.agent.sbwnpcaddon.menu.NpcTradingSetupMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NpcTradingSetupScreen extends AbstractContainerScreen<NpcTradingSetupMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/crafting_table.png");

    public NpcTradingSetupScreen(NpcTradingSetupMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
""")

write_file("src/main/java/com/agent/sbwnpcaddon/client/screen/NpcTradingScreen.java", """package com.agent.sbwnpcaddon.client.screen;

import com.agent.sbwnpcaddon.menu.NpcTradingMenu;
import com.agent.sbwnpcaddon.network.SbwNetwork;
import com.agent.sbwnpcaddon.network.TradeCompletePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NpcTradingScreen extends AbstractContainerScreen<NpcTradingMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/crafting_table.png");

    public NpcTradingScreen(NpcTradingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("Trade"), b -> {
            SbwNetwork.CHANNEL.sendToServer(new TradeCompletePacket());
        }).bounds(this.leftPos + 85, this.topPos + 60, 40, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
""")

write_file("src/main/java/com/agent/sbwnpcaddon/network/TradeCompletePacket.java", """package com.agent.sbwnpcaddon.network;

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
        return true;
    }
}
""")
