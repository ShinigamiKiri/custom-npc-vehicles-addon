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
    private final List<Boolean> selected;

    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;

    private EditBox pxBox;
    private EditBox pyBox;
    private EditBox pzBox;
    
    private int mode = 2; // Default to Move
    private String[] modeNames = new String[] {"Follow", "Stay / Guard", "Move", "Patrol"};
    private String[] presetNames = new String[] {"", "Preset 1: Proximity", "Preset 2: Retaliate", "Preset 3: Tank"};

    public CommandDeviceScreen(List<Integer> ids, List<String> names, List<Integer> presets) {
        super(Component.literal("Command Device"));
        this.ids = ids;
        this.names = names;
        this.presets = presets;
        this.selected = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            this.selected.add(false);
        }
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;

        xBox = new EditBox(this.font, cx - 110, cy - 60, 60, 20, Component.empty());
        yBox = new EditBox(this.font, cx - 40, cy - 60, 60, 20, Component.empty());
        zBox = new EditBox(this.font, cx + 30, cy - 60, 60, 20, Component.empty());
        this.addRenderableWidget(xBox);
        this.addRenderableWidget(yBox);
        this.addRenderableWidget(zBox);

        pxBox = new EditBox(this.font, cx - 110, cy - 20, 60, 20, Component.empty());
        pyBox = new EditBox(this.font, cx - 40, cy - 20, 60, 20, Component.empty());
        pzBox = new EditBox(this.font, cx + 30, cy - 20, 60, 20, Component.empty());
        this.addRenderableWidget(pxBox);
        this.addRenderableWidget(pyBox);
        this.addRenderableWidget(pzBox);

        updateVisibility();

        this.addRenderableWidget(Button.builder(Component.literal("Mode: " + modeNames[mode]), b -> {
            mode = (mode + 1) % modeNames.length;
            b.setMessage(Component.literal("Mode: " + modeNames[mode]));
            updateVisibility();
        }).bounds(cx + 100, cy - 60, 110, 20).build());

        int listY = cy + 10;
        for (int i = 0; i < names.size(); i++) {
            final int index = i;
            
            // Selection Button
            this.addRenderableWidget(Button.builder(Component.literal((selected.get(i) ? "[X] " : "[ ] ") + names.get(i)), b -> {
                selected.set(index, !selected.get(index));
                b.setMessage(Component.literal((selected.get(index) ? "[X] " : "[ ] ") + names.get(index)));
            }).bounds(cx - 150, listY + i * 22, 160, 20).build());
            
            // Preset Toggle Button
            int initialPreset = presets.get(i) >= 1 && presets.get(i) <= 3 ? presets.get(i) : 1;
            this.addRenderableWidget(Button.builder(Component.literal(presetNames[initialPreset]), b -> {
                int current = presets.get(index);
                current = current >= 3 ? 1 : current + 1;
                presets.set(index, current);
                b.setMessage(Component.literal(presetNames[current]));
                
                // Immediately send update to server
                SbwNetwork.CHANNEL.sendToServer(new UpdateCombatPresetPacket(ids.get(index), current));
                
            }).bounds(cx + 15, listY + i * 22, 130, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("Execute Command"), b -> {
            executeCommand(false);
        }).bounds(cx + 100, cy + 10, 110, 20).build());
        
        this.addRenderableWidget(Button.builder(Component.literal("Cancel / Reset To Default"), b -> {
            executeCommand(true);
        }).bounds(cx + 100, cy + 35, 180, 20).build());
    }

    private void updateVisibility() {
        boolean needsPointA = (mode == 2 || mode == 3);
        boolean needsPointB = (mode == 3);
        xBox.visible = needsPointA;
        yBox.visible = needsPointA;
        zBox.visible = needsPointA;
        pxBox.visible = needsPointB;
        pyBox.visible = needsPointB;
        pzBox.visible = needsPointB;
    }

    private void executeCommand(boolean cancel) {
        try {
            double x = 0, y = 0, z = 0;
            double px = 0, py = 0, pz = 0;
            
            if (!cancel) {
                if (mode == 2 || mode == 3) {
                    x = Double.parseDouble(xBox.getValue());
                    y = Double.parseDouble(yBox.getValue());
                    z = Double.parseDouble(zBox.getValue());
                }
                px = x; py = y; pz = z;
                if (mode == 3) {
                    px = Double.parseDouble(pxBox.getValue());
                    py = Double.parseDouble(pyBox.getValue());
                    pz = Double.parseDouble(pzBox.getValue());
                }
            }

            List<Integer> selectedIds = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                if (selected.get(i)) {
                    selectedIds.add(ids.get(i));
                }
            }

            if (!selectedIds.isEmpty()) {
                SbwNetwork.CHANNEL.sendToServer(new IssueCommandDevicePacket(selectedIds, cancel, mode, x, y, z, px, py, pz));
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
        
        guiGraphics.drawString(this.font, "Note: List may be incomplete due to unloaded chunks.", cx - 110, cy - 90, 0xFFAA00, false);

        if (mode == 2 || mode == 3) {
            guiGraphics.drawString(this.font, "Point A (X, Y, Z)", cx - 110, cy - 75, 0xFFFFFF, true);
        }
        if (mode == 3) {
            guiGraphics.drawString(this.font, "Point B (X, Y, Z)", cx - 110, cy - 35, 0xFFFFFF, true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
