package com.agent.sbwnpcaddon.client.screen;

import com.agent.sbwnpcaddon.network.SaveVehicleConfigPacket;
import com.agent.sbwnpcaddon.network.SbwNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class VehicleConfigScreen extends Screen {
    private final LivingEntity entity;
    
    private int type;
    private EditBox maxSpeedBox;
    private EditBox accelBox;
    private EditBox brakeBox;
    private EditBox turnRadBox;
    private boolean physicsEnabled;

    public VehicleConfigScreen(LivingEntity entity) {
        super(Component.literal("Vehicle Configuration"));
        this.entity = entity;
        
        this.type = entity.getPersistentData().getInt("SbwVehicleType");
        float ms = entity.getPersistentData().contains("SbwMaxSpeed") ? entity.getPersistentData().getFloat("SbwMaxSpeed") : 1.5f;
        float acc = entity.getPersistentData().contains("SbwAcceleration") ? entity.getPersistentData().getFloat("SbwAcceleration") : 0.02f;
        float brk = entity.getPersistentData().contains("SbwBraking") ? entity.getPersistentData().getFloat("SbwBraking") : 0.05f;
        float tr = entity.getPersistentData().contains("SbwTurnRadius") ? entity.getPersistentData().getFloat("SbwTurnRadius") : 2.0f;
        this.physicsEnabled = entity.getPersistentData().getBoolean("SbwPhysicsEnabled");
        
        maxSpeedBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());
        accelBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());
        brakeBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());
        turnRadBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());
        
        maxSpeedBox.setValue(String.valueOf(ms));
        accelBox.setValue(String.valueOf(acc));
        brakeBox.setValue(String.valueOf(brk));
        turnRadBox.setValue(String.valueOf(tr));
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;
        
        maxSpeedBox.setX(cx - 50); maxSpeedBox.setY(cy - 60);
        accelBox.setX(cx - 50); accelBox.setY(cy - 35);
        brakeBox.setX(cx - 50); brakeBox.setY(cy - 10);
        turnRadBox.setX(cx - 50); turnRadBox.setY(cy + 15);
        
        this.addRenderableWidget(maxSpeedBox);
        this.addRenderableWidget(accelBox);
        this.addRenderableWidget(brakeBox);
        this.addRenderableWidget(turnRadBox);
        
        // Type cycle button
        this.addRenderableWidget(Button.builder(Component.literal(getTypeName()), b -> {
            type = (type + 1) % 4;
            b.setMessage(Component.literal(getTypeName()));
        }).bounds(cx - 50, cy - 85, 100, 20).build());
        
        // Physics toggle
        this.addRenderableWidget(Button.builder(Component.literal("Physics: " + (physicsEnabled ? "ON" : "OFF")), b -> {
            physicsEnabled = !physicsEnabled;
            b.setMessage(Component.literal("Physics: " + (physicsEnabled ? "ON" : "OFF")));
        }).bounds(cx - 50, cy + 40, 100, 20).build());
        
        // Save
        this.addRenderableWidget(Button.builder(Component.literal("Save/Apply"), b -> {
            try {
                float ms = Float.parseFloat(maxSpeedBox.getValue());
                float acc = Float.parseFloat(accelBox.getValue());
                float brk = Float.parseFloat(brakeBox.getValue());
                float tr = Float.parseFloat(turnRadBox.getValue());
                SbwNetwork.CHANNEL.sendToServer(new SaveVehicleConfigPacket(entity.getId(), type, ms, acc, brk, tr, physicsEnabled));
                this.minecraft.setScreen(null);
            } catch (Exception e) {
                // validation failed, don't save
            }
        }).bounds(cx - 105, cy + 65, 100, 20).build());
        
        // Cancel
        this.addRenderableWidget(Button.builder(Component.literal("Cancel/Close"), b -> {
            this.minecraft.setScreen(null);
        }).bounds(cx + 5, cy + 65, 100, 20).build());
    }

    private String getTypeName() {
        return switch(type) {
            case 0 -> "Ground Vehicle";
            case 1 -> "Boat";
            case 2 -> "Plane";
            case 3 -> "Helicopter";
            default -> "Ground Vehicle";
        };
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int cx = this.width / 2;
        int cy = this.height / 2;
        guiGraphics.drawString(this.font, "Max Speed", cx - 110, cy - 55, 0xFFFFFF, true);
        guiGraphics.drawString(this.font, "Accel Rate", cx - 110, cy - 30, 0xFFFFFF, true);
        guiGraphics.drawString(this.font, "Brake Rate", cx - 110, cy - 5, 0xFFFFFF, true);
        guiGraphics.drawString(this.font, "Turn Radius", cx - 110, cy + 20, 0xFFFFFF, true);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
