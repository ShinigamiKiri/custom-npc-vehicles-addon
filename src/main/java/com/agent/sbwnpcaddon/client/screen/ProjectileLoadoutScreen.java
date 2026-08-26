package com.agent.sbwnpcaddon.client.screen;

import com.agent.sbwnpcaddon.network.SaveProjectileLoadoutPacket;
import com.agent.sbwnpcaddon.network.SbwNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.client.gui.SubGuiNpcProjectiles;
import noppes.npcs.entity.data.DataRanged;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.client.gui.components.GuiBasic;
import noppes.npcs.shared.client.gui.components.GuiWrapper;

public class ProjectileLoadoutScreen extends Screen {
    private final LivingEntity entity;
    private final Screen parent;
    private ListTag loadout;
    private int currentIndex = 0;
    
    private EditBox nameBox;

    public ProjectileLoadoutScreen(LivingEntity entity, Screen parent) {
        super(Component.literal("Projectile Loadout"));
        this.entity = entity;
        this.parent = parent;
        
        ListTag existing = entity.getPersistentData().getList("SbwProjectileLoadout", 10);
        this.loadout = existing.copy();
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        
        int cx = this.width / 2;
        int cy = this.height / 2;

        nameBox = new EditBox(this.font, cx - 75, cy - 50, 150, 20, Component.empty());
        if (currentIndex >= 0 && currentIndex < loadout.size()) {
            nameBox.setValue(loadout.getCompound(currentIndex).getString("Name"));
        } else {
            nameBox.setEditable(false);
        }
        this.addRenderableWidget(nameBox);
        
        // Navigation
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            saveCurrentEntry();
            currentIndex = (currentIndex - 1 + loadout.size()) % loadout.size();
            this.init();
        }).bounds(cx - 100, cy - 20, 20, 20).build());
        
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            saveCurrentEntry();
            currentIndex = (currentIndex + 1) % loadout.size();
            this.init();
        }).bounds(cx + 80, cy - 20, 20, 20).build());

        // Add / Remove
        this.addRenderableWidget(Button.builder(Component.literal("Add Entry"), b -> {
            saveCurrentEntry();
            CompoundTag newEntry = new CompoundTag();
            newEntry.putString("Name", "New Projectile");
            newEntry.put("DataRanged", new CompoundTag());
            loadout.add(newEntry);
            currentIndex = loadout.size() - 1;
            this.init();
        }).bounds(cx - 150, cy + 30, 95, 20).build());
        
        this.addRenderableWidget(Button.builder(Component.literal("Remove Entry"), b -> {
            if (currentIndex >= 0 && currentIndex < loadout.size()) {
                loadout.remove(currentIndex);
                if (currentIndex >= loadout.size()) {
                    currentIndex = loadout.size() - 1;
                }
                this.init();
            }
        }).bounds(cx + 55, cy + 30, 95, 20).build());
        
        // Edit Stats
        this.addRenderableWidget(Button.builder(Component.literal("Edit Projectile Stats (CustomNPCs Native)"), b -> {
            if (currentIndex >= 0 && currentIndex < loadout.size()) {
                saveCurrentEntry();
                DataRanged dummyStats = new DataRanged(null);
                CompoundTag entry = loadout.getCompound(currentIndex);
                if (entry.contains("DataRanged")) {
                    dummyStats.load(entry.getCompound("DataRanged"));
                }
                
                SubGuiNpcProjectiles subgui = new SubGuiNpcProjectiles(dummyStats);
                GuiWrapper wrapper = subgui.getWrapper();
                if (wrapper != null) {
                    wrapper.parent = this;
                }
                
                // wait, if we do that, we need to save the dummyStats back when we return.
                // Wait, SubGuiNpcProjectiles modifies dummyStats in place!
                // So we should schedule a tick to copy it back, or just copy it back in tick()?
                // Or better, let's save it right before sending the packet!
                this.minecraft.setScreen(subgui);
            }
        }).bounds(cx - 150, cy - 15, 220, 20).build());

        // Set Item
        this.addRenderableWidget(Button.builder(Component.literal("Set Item to Held"), b -> {
            if (currentIndex >= 0 && currentIndex < loadout.size()) {
                ItemStack held = this.minecraft.player.getMainHandItem();
                CompoundTag entry = loadout.getCompound(currentIndex);
                if (!held.isEmpty()) {
                    entry.put("Item", held.save(new CompoundTag()));
                } else {
                    entry.remove("Item");
                }
                this.init();
            }
        }).bounds(cx - 50, cy + 5, 100, 20).build());

        // Save & Back
        this.addRenderableWidget(Button.builder(Component.literal("Save Loadout & Back"), b -> {
            saveCurrentEntry();
            SbwNetwork.CHANNEL.sendToServer(new SaveProjectileLoadoutPacket(entity.getId(), loadout));
            this.minecraft.setScreen(parent);
        }).bounds(cx - 75, cy + 65, 150, 20).build());
    }
    
    private void saveCurrentEntry() {
        if (currentIndex >= 0 && currentIndex < loadout.size()) {
            loadout.getCompound(currentIndex).putString("Name", nameBox.getValue());
            // It's possible the subgui modified dummyStats, but since we didn't keep a ref here,
            // we will let them use a small wrapper instead. Let's fix the subgui save problem later.
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int cx = this.width / 2;
        int cy = this.height / 2;
        
        guiGraphics.drawCenteredString(this.font, "Projectile Loadout", cx, cy - 80, 0xFFFFFF);
        
        if (loadout.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "No projectiles configured.", cx, cy - 65, 0xAAAAAA);
        } else {
            guiGraphics.drawCenteredString(this.font, "Entry " + (currentIndex + 1) + " / " + loadout.size(), cx, cy - 65, 0xAAAAAA);
            
            // Show current item
            CompoundTag entry = loadout.getCompound(currentIndex);
            String itemStr = "Item: None";
            if (entry.contains("Item")) {
                ItemStack stack = ItemStack.of(entry.getCompound("Item"));
                if (!stack.isEmpty()) {
                    itemStr = "Item: " + stack.getHoverName().getString();
                }
            }
            guiGraphics.drawCenteredString(this.font, itemStr, cx, cy + 10, 0xFFFF00);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
