package com.agent.sbwnpcaddon.client;

import net.minecraft.client.Minecraft;
import java.util.List;

public class ClientHelper {
    public static void openCommandDeviceScreen(List<Integer> ids, List<String> names, List<Integer> presets) {
        Minecraft.getInstance().setScreen(new com.agent.sbwnpcaddon.client.screen.CommandDeviceScreen(ids, names, presets));
    }
}
