package com.agent.sbwnpcaddon.client.screen;

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
        
        // Render ghost items for requirements
        var handler = this.menu.blockEntity.getItemHandler();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int slotIndex = j + i * 3;
                ItemStack req = handler.getStackInSlot(slotIndex);
                ItemStack prov = this.menu.tradeInput.getStackInSlot(slotIndex);
                if (!req.isEmpty() && prov.isEmpty()) {
                    int x = this.leftPos + 30 + j * 18;
                    int y = this.topPos + 17 + i * 18;
                    
                    // Render translucent item? Just render item
                    pGuiGraphics.renderItem(req, x, y);
                    pGuiGraphics.renderItemDecorations(this.font, req, x, y);
                    
                    // Draw a semi-transparent dark overlay to make it look like a ghost item
                    pGuiGraphics.fill(x, y, x + 16, y + 16, 0x80000000);
                }
            }
        }
        
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
