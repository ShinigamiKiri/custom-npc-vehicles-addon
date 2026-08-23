package com.agent.sbwnpcaddon.client.screen;

import com.agent.sbwnpcaddon.network.IssueCommandDevicePacket;
import com.agent.sbwnpcaddon.network.UpdateCombatPresetPacket;
import com.agent.sbwnpcaddon.network.SbwNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CommandDeviceScreen extends Screen {
    private final List<Integer> ids;
    private final List<String> names;
    private final List<Integer> presets;
    private final List<Boolean> isCommandActive;
    private final List<Integer> activeModes;
    private final List<Double> targetXs, targetYs, targetZs;
    private final List<Double> targetX2s, targetY2s, targetZ2s;
    
    private final List<Boolean> selected;

    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;

    private EditBox pxBox;
    private EditBox pyBox;
    private EditBox pzBox;
    
    // 0 = List view, 1 = Single NPC edit, 2 = Batch edit
    private int viewState = 0; 
    private int editingIndex = -1; // Which NPC is being edited in state 1
    
    private int tempMode = 0; 
    
    private String[] modeNames = new String[] {"Follow", "Stay", "Move", "Patrol", "Guard", "Patrol-Guard"};
    private String[] presetNames = new String[] {"", "Preset 1: Proximity", "Preset 2: Retaliate", "Preset 3: Tank"};

    public CommandDeviceScreen(List<Integer> ids, List<String> names, List<Integer> presets,
                               List<Boolean> isCommandActive, List<Integer> activeModes,
                               List<Double> targetXs, List<Double> targetYs, List<Double> targetZs,
                               List<Double> targetX2s, List<Double> targetY2s, List<Double> targetZ2s) {
        super(Component.literal("Command Device"));
        this.ids = ids;
        this.names = names;
        this.presets = presets;
        this.isCommandActive = isCommandActive;
        this.activeModes = activeModes;
        this.targetXs = targetXs; this.targetYs = targetYs; this.targetZs = targetZs;
        this.targetX2s = targetX2s; this.targetY2s = targetY2s; this.targetZ2s = targetZ2s;
        
        this.selected = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            this.selected.add(false);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        
        int cx = this.width / 2;
        int cy = this.height / 2;

        if (viewState == 0) {
            // Main Follower List View
            int listY = cy - 80;
            for (int i = 0; i < names.size(); i++) {
                final int index = i;
                
                // Checkbox for batch
                this.addRenderableWidget(Button.builder(Component.literal(selected.get(i) ? "[X]" : "[ ]"), b -> {
                    selected.set(index, !selected.get(index));
                    b.setMessage(Component.literal(selected.get(index) ? "[X]" : "[ ]"));
                }).bounds(cx - 130, listY + i * 22, 20, 20).build());
                
                // Name button to edit this specific NPC
                String status = isCommandActive.get(i) ? " (" + modeNames[activeModes.get(i)] + ")" : " (Idle)";
                this.addRenderableWidget(Button.builder(Component.literal(names.get(i) + status), b -> {
                    editingIndex = index;
                    tempMode = isCommandActive.get(index) ? activeModes.get(index) : 0;
                    viewState = 1;
                    this.init();
                }).bounds(cx - 105, listY + i * 22, 235, 20).build());
            }
            
            this.addRenderableWidget(Button.builder(Component.literal("Batch Command Selected"), b -> {
                tempMode = 0;
                viewState = 2;
                this.init();
            }).bounds(cx - 75, listY + names.size() * 22 + 10, 150, 20).build());
            
        } else {
            // Single or Batch Edit View
            xBox = new EditBox(this.font, cx - 110, cy - 30, 60, 20, Component.empty());
            yBox = new EditBox(this.font, cx - 40, cy - 30, 60, 20, Component.empty());
            zBox = new EditBox(this.font, cx + 30, cy - 30, 60, 20, Component.empty());
            this.addRenderableWidget(xBox);
            this.addRenderableWidget(yBox);
            this.addRenderableWidget(zBox);

            pxBox = new EditBox(this.font, cx - 110, cy + 10, 60, 20, Component.empty());
            pyBox = new EditBox(this.font, cx - 40, cy + 10, 60, 20, Component.empty());
            pzBox = new EditBox(this.font, cx + 30, cy + 10, 60, 20, Component.empty());
            this.addRenderableWidget(pxBox);
            this.addRenderableWidget(pyBox);
            this.addRenderableWidget(pzBox);
            
            if (viewState == 1 && isCommandActive.get(editingIndex)) {
                xBox.setValue(String.valueOf(targetXs.get(editingIndex)));
                yBox.setValue(String.valueOf(targetYs.get(editingIndex)));
                zBox.setValue(String.valueOf(targetZs.get(editingIndex)));
                pxBox.setValue(String.valueOf(targetX2s.get(editingIndex)));
                pyBox.setValue(String.valueOf(targetY2s.get(editingIndex)));
                pzBox.setValue(String.valueOf(targetZ2s.get(editingIndex)));
            }

            this.addRenderableWidget(Button.builder(Component.literal("Mode: " + modeNames[tempMode]), b -> {
                tempMode = (tempMode + 1) % modeNames.length;
                b.setMessage(Component.literal("Mode: " + modeNames[tempMode]));
                updateVisibility();
            }).bounds(cx - 55, cy - 65, 110, 20).build());
            
            if (viewState == 1) {
                int initialPreset = presets.get(editingIndex) >= 1 && presets.get(editingIndex) <= 3 ? presets.get(editingIndex) : 1;
                this.addRenderableWidget(Button.builder(Component.literal(presetNames[initialPreset]), b -> {
                    int current = presets.get(editingIndex);
                    current = current >= 3 ? 1 : current + 1;
                    presets.set(editingIndex, current);
                    b.setMessage(Component.literal(presetNames[current]));
                    SbwNetwork.CHANNEL.sendToServer(new UpdateCombatPresetPacket(ids.get(editingIndex), current));
                }).bounds(cx - 65, cy + 40, 130, 20).build());
            }

            this.addRenderableWidget(Button.builder(Component.literal("Execute"), b -> {
                executeCommand(false);
            }).bounds(cx + 60, cy + 70, 70, 20).build());
            
            this.addRenderableWidget(Button.builder(Component.literal("Cancel Cmd"), b -> {
                executeCommand(true);
            }).bounds(cx - 15, cy + 70, 70, 20).build());
            
            this.addRenderableWidget(Button.builder(Component.literal("< Back"), b -> {
                viewState = 0;
                this.init();
            }).bounds(cx - 130, cy + 70, 50, 20).build());
            
            updateVisibility();
        }
    }

    private void updateVisibility() {
        if (viewState != 0) {
            boolean needsPointA = (tempMode == 2 || tempMode == 3);
            boolean needsPointB = (tempMode == 3);
            xBox.visible = needsPointA;
            yBox.visible = needsPointA;
            zBox.visible = needsPointA;
            pxBox.visible = needsPointB;
            pyBox.visible = needsPointB;
            pzBox.visible = needsPointB;
        }
    }

    private void executeCommand(boolean cancel) {
        try {
            double x = 0, y = 0, z = 0;
            double px = 0, py = 0, pz = 0;
            
            if (!cancel) {
                if (tempMode == 2 || tempMode == 3) {
                    x = Double.parseDouble(xBox.getValue());
                    y = Double.parseDouble(yBox.getValue());
                    z = Double.parseDouble(zBox.getValue());
                }
                px = x; py = y; pz = z;
                if (tempMode == 3) {
                    px = Double.parseDouble(pxBox.getValue());
                    py = Double.parseDouble(pyBox.getValue());
                    pz = Double.parseDouble(pzBox.getValue());
                }
            }

            List<Integer> selectedIds = new ArrayList<>();
            if (viewState == 1) {
                selectedIds.add(ids.get(editingIndex));
            } else if (viewState == 2) {
                for (int i = 0; i < ids.size(); i++) {
                    if (selected.get(i)) {
                        selectedIds.add(ids.get(i));
                    }
                }
            }

            if (!selectedIds.isEmpty()) {
                SbwNetwork.CHANNEL.sendToServer(new IssueCommandDevicePacket(selectedIds, cancel, tempMode, x, y, z, px, py, pz));
            }
            this.minecraft.setScreen(null);
        } catch (Exception e) {
            // invalid input
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        int cy = this.height / 2;
        
        if (viewState == 0) {
            guiGraphics.drawString(this.font, "Select NPCs to Command", cx - 60, cy - 100, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, "Note: List may be incomplete due to unloaded chunks.", cx - 110, cy + 90, 0xFFAA00, false);
        } else {
            String title = viewState == 1 ? "Commanding: " + names.get(editingIndex) : "Batch Command Selected NPCs";
            guiGraphics.drawString(this.font, title, cx - this.font.width(title) / 2, cy - 90, 0x00FF00, false);
            
            if (viewState == 1) {
                String curCmd = isCommandActive.get(editingIndex) ? "Currently Active: " + modeNames[activeModes.get(editingIndex)] : "Currently Active: None";
                guiGraphics.drawString(this.font, curCmd, cx - this.font.width(curCmd) / 2, cy - 80, 0xAAAAAA, false);
            }

            if (tempMode == 2 || tempMode == 3) {
                guiGraphics.drawString(this.font, "Point A (X, Y, Z)", cx - 110, cy - 45, 0xFFFFFF, true);
            }
            if (tempMode == 3) {
                guiGraphics.drawString(this.font, "Point B (X, Y, Z)", cx - 110, cy - 5, 0xFFFFFF, true);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
