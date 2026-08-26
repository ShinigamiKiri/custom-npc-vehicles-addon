package com.agent.sbwnpcaddon.client;

import com.agent.sbwnpcaddon.client.screen.CommandDeviceScreen;
import net.minecraft.client.Minecraft;
import java.util.List;

public class ClientHelper {
    public static void openCommandDeviceScreen(List<Integer> ids, List<String> names, List<Integer> presets,
                                               List<Boolean> isCommandActive, List<Integer> activeModes,
                                               List<Double> targetXs, List<Double> targetYs, List<Double> targetZs,
                                               List<Double> targetX2s, List<Double> targetY2s, List<Double> targetZ2s,
                                               List<String> projectileLoadoutNames, List<Integer> activeProjectileIndices) {
        Minecraft.getInstance().setScreen(new CommandDeviceScreen(ids, names, presets, isCommandActive, activeModes,
                targetXs, targetYs, targetZs, targetX2s, targetY2s, targetZ2s, projectileLoadoutNames, activeProjectileIndices));
    }
}

