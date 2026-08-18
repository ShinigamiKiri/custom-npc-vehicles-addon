package com.agent.sbwnpcaddon.entity;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;

@Mod.EventBusSubscriber(modid = "sbw_npc_addon", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        EntityRegistry.ENTITIES.getEntries().forEach(entityTypeRegistryObject -> {
            event.put((EntityType<? extends PathfinderMob>) entityTypeRegistryObject.get(), SbwNpcEntity.createAttributes().build());
        });
    }
}
