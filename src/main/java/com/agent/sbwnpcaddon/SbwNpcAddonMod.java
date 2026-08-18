package com.agent.sbwnpcaddon;

import com.agent.sbwnpcaddon.entity.EntityRegistry;
import com.agent.sbwnpcaddon.sound.SoundRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.agent.sbwnpcaddon.item.ItemRegistry;
import com.agent.sbwnpcaddon.item.VehicleConfigTool;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;

@Mod("sbw_npc_addon")
public class SbwNpcAddonMod {
    public SbwNpcAddonMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EntityRegistry.register(modEventBus);
        SoundRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        
        MinecraftForge.EVENT_BUS.addListener(this::onLivingTick);
    }
    
    private void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().getPersistentData().getBoolean("SbwPhysicsEnabled")) {
            var module = VehicleConfigTool.physicsModules.get(event.getEntity());
            if (module == null && event.getEntity() instanceof net.minecraft.world.entity.Mob) {
                module = new com.agent.sbwnpcaddon.entity.physics.SbwPhysicsModule((net.minecraft.world.entity.Mob) event.getEntity());
                VehicleConfigTool.physicsModules.put(event.getEntity(), module);
            }
            if (module != null) {
                // Dummy values for testing, real input would be sent via packets
                module.tickSteering(true, false, false, false); 
            }
        }
    }
}
