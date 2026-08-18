package com.agent.sbwnpcaddon.entity;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import com.agent.sbwnpcaddon.entity.IvNpcEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;

@Mod.EventBusSubscriber(modid = "sbw_npc_addon", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        EntityRegistry.ENTITIES.getEntries().forEach(entityTypeRegistryObject -> {
            if (!entityTypeRegistryObject.getId().getPath().startsWith("iv_")) {
                event.put((EntityType<? extends PathfinderMob>) entityTypeRegistryObject.get(), SbwNpcEntity.createAttributes().build());
            }
        });
        event.put(EntityRegistry.CHARGER.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.CUSTOMBENCH.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.DECORBENCH.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.ENGINEBENCH.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.FUELPUMP.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.GUNBENCH.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.INSTRUMENTBENCH.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.INVISIBLE.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.ITEMBENCH.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.JERRYCAN.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.PROPELLERBENCH.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.SEATBENCH.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.VEHICLEBENCH.get(), IvNpcEntity.createAttributes().build());
        event.put(EntityRegistry.WHEELBENCH.get(), IvNpcEntity.createAttributes().build());
    }
}
