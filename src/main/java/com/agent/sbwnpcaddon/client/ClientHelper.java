package com.agent.sbwnpcaddon.client;

import com.agent.sbwnpcaddon.client.screen.CommandDeviceScreen;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ClientHelper {
    public static void openCommandDeviceScreen(List<Integer> ids, List<String> names) {
        Minecraft.getInstance().setScreen(new CommandDeviceScreen(ids, names));
    }
}
