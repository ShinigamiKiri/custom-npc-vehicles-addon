package com.agent.sbwnpcaddon.client;

import net.minecraft.client.Minecraft;
import java.util.List;

public class ClientHelper {
    public static void openCommandDeviceScreen(java.util.List<Integer> ids, java.util.List<String> names, java.util.List<Integer> presets,
                                               java.util.List<Boolean> isCommandActive, java.util.List<Integer> activeModes,
                                               java.util.List<Double> targetXs, java.util.List<Double> targetYs, java.util.List<Double> targetZs,
                                               java.util.List<Double> targetX2s, java.util.List<Double> targetY2s, java.util.List<Double> targetZ2s) {
        Minecraft.getInstance().setScreen(new com.agent.sbwnpcaddon.client.screen.CommandDeviceScreen(ids, names, presets, isCommandActive, activeModes, targetXs, targetYs, targetZs, targetX2s, targetY2s, targetZ2s));
    }
}
