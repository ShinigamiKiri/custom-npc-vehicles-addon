package com.agent.sbwnpcaddon.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Assuming Custom NPCs uses standard Screen or a custom base class that we can hook into init()
@Mixin(targets = "noppes.npcs.client.gui.mainmenu.GuiNpcAI", remap = false)
public class GuiNpcAIMixin {

    @Inject(method = "init()V", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Injecting into the AI tab to add physics options for the SbwPhysicsModule
        Object self = this;
        if (self instanceof net.minecraft.client.gui.screens.Screen) {
            net.minecraft.client.gui.screens.Screen screen = (net.minecraft.client.gui.screens.Screen) self;
            
            // Add a new button to toggle SbwPhysicsModule logic
            // In a real scenario, this would send a packet to update the entity's NBT
            Button physicsButton = Button.builder(Component.literal("Vehicle Physics: ON"), button -> {
                // Toggle logic here
            }).bounds(screen.width / 2 - 100, screen.height / 2 + 50, 200, 20).build();
            
            // We use reflection or access wideners to add the button if addRenderableWidget is protected,
            // but in Forge 1.20.1 standard mappings it's usually accessible or we can use the screen's widget list.
            // For safety in mixins without ATs:
            try {
                java.lang.reflect.Method m = net.minecraft.client.gui.screens.Screen.class.getDeclaredMethod("addRenderableWidget", net.minecraft.client.gui.components.events.GuiEventListener.class);
                m.setAccessible(true);
                m.invoke(screen, physicsButton);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
