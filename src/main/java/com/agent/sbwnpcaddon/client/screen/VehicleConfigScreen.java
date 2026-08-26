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
    private int aircraftMode;
    private EditBox maxSpeedBox;
    private EditBox accelBox;
    private EditBox brakeBox;
    private EditBox turnRadBox;
    private boolean physicsEnabled;
    private Button aircraftModeButton;
    private float modelYawOffset;

    public VehicleConfigScreen(LivingEntity entity) {
        super(Component.literal("Vehicle Configuration"));
        this.entity = entity;
        
        this.type = entity.getPersistentData().getInt("SbwVehicleType");
        if (entity.getPersistentData().contains("SbwAircraftMode")) {
            this.aircraftMode = entity.getPersistentData().getInt("SbwAircraftMode");
        } else {
            this.aircraftMode = (type == 3) ? 0 : 1;
        }
        this.physicsEnabled = entity.getPersistentData().getBoolean("SbwPhysicsEnabled");
        this.modelYawOffset = entity.getPersistentData().getFloat("SbwModelYawOffset");
    }

    @Override
    protected void init() {
        super.init();
        
        maxSpeedBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());
        accelBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());
        brakeBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());
        turnRadBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty());
        
        float ms = entity.getPersistentData().contains("SbwMaxSpeed") ? entity.getPersistentData().getFloat("SbwMaxSpeed") : 0.5f;
        float acc = entity.getPersistentData().contains("SbwAcceleration") ? entity.getPersistentData().getFloat("SbwAcceleration") : 0.005f;
        float brk = entity.getPersistentData().contains("SbwBraking") ? entity.getPersistentData().getFloat("SbwBraking") : 0.02f;
        float tr = entity.getPersistentData().contains("SbwTurnRadius") ? entity.getPersistentData().getFloat("SbwTurnRadius") : 1.0f;
        
        maxSpeedBox.setValue(String.valueOf(ms));
        accelBox.setValue(String.valueOf(acc));
        brakeBox.setValue(String.valueOf(brk));
        turnRadBox.setValue(String.valueOf(tr));
        
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
            updateAircraftModeButton();
        }).bounds(cx - 155, cy - 85, 100, 20).build());
        
        // Aircraft Mode cycle button
        aircraftModeButton = this.addRenderableWidget(Button.builder(Component.literal(getAircraftModeName()), b -> {
            aircraftMode = (aircraftMode + 1) % 2;
            b.setMessage(Component.literal(getAircraftModeName()));
        }).bounds(cx + 55, cy - 85, 100, 20).build());
        updateAircraftModeButton();
        
        // Physics toggle
        this.addRenderableWidget(Button.builder(Component.literal("Physics: " + (physicsEnabled ? "ON" : "OFF")), b -> {
            physicsEnabled = !physicsEnabled;
            b.setMessage(Component.literal("Physics: " + (physicsEnabled ? "ON" : "OFF")));
        }).bounds(cx - 155, cy + 40, 100, 20).build());

        // Model Yaw Offset
        this.addRenderableWidget(Button.builder(Component.literal("Model Yaw: " + (int)modelYawOffset), b -> {
            modelYawOffset = (modelYawOffset + 90) % 360;
            b.setMessage(Component.literal("Model Yaw: " + (int)modelYawOffset));
        }).bounds(cx - 50, cy + 40, 100, 20).build());
        
        // Save (Current entity only)
        this.addRenderableWidget(Button.builder(Component.literal("Save/Apply"), b -> {
            save(false);
        }).bounds(cx - 155, cy + 65, 100, 20).build());
        
        // Save (All clones)
        this.addRenderableWidget(Button.builder(Component.literal("Apply to All Clones"), b -> {
            save(true);
        }).bounds(cx - 50, cy + 65, 100, 20).build());
        
        // Cancel
        this.addRenderableWidget(Button.builder(Component.literal("Cancel/Close"), b -> {
            this.minecraft.setScreen(null);
        }).bounds(cx + 55, cy + 65, 100, 20).build());

        // Projectile Loadout
        this.addRenderableWidget(Button.builder(Component.literal("Projectile Loadout"), b -> {
            this.minecraft.setScreen(new ProjectileLoadoutScreen(this.entity, this));
        }).bounds(cx - 50, cy - 85, 100, 20).build());
    }
    
    private void save(boolean applyToAll) {
        try {
            float ms = Float.parseFloat(maxSpeedBox.getValue());
            float acc = Float.parseFloat(accelBox.getValue());
            float brk = Float.parseFloat(brakeBox.getValue());
            float tr = Float.parseFloat(turnRadBox.getValue());
            SbwNetwork.CHANNEL.sendToServer(new SaveVehicleConfigPacket(entity.getId(), type, ms, acc, brk, tr, aircraftMode, physicsEnabled, modelYawOffset, applyToAll));
            this.minecraft.setScreen(null);
        } catch (Exception e) {
            // validation failed, don't save
        }
    }

    private String getTypeName() {
        return switch(type) {
            case 0 -> "Ground Vehicle";
            case 1 -> "Boat";
            case 2 -> "Aircraft";
            case 3 -> "Aircraft (Old Heli)";
            default -> "Ground Vehicle";
        };
    }

    private void updateAircraftModeButton() {
        if (aircraftModeButton != null) {
            aircraftModeButton.visible = (type == 2 || type == 3);
        }
    }

    private String getAircraftModeName() {
        return aircraftMode == 0 ? "Hover/Stationary" : "Runway Takeoff";
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
