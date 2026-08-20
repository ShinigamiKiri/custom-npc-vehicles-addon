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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.InteractionResult;

@Mod("sbw_npc_addon")
public class SbwNpcAddonMod {
    public SbwNpcAddonMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EntityRegistry.register(modEventBus);
        SoundRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        
        MinecraftForge.EVENT_BUS.addListener(this::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onEntityInteractSpecific);
        
        com.agent.sbwnpcaddon.network.SbwNetwork.register();
    }
    
    private void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().getItem() == ItemRegistry.VEHICLE_CONFIG_TOOL.get()) {
            if (!event.getLevel().isClientSide && event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target) {
                int type = target.getPersistentData().getInt("SbwVehicleType");
                float ms = target.getPersistentData().contains("SbwMaxSpeed") ? target.getPersistentData().getFloat("SbwMaxSpeed") : 0.5f;
                float acc = target.getPersistentData().contains("SbwAcceleration") ? target.getPersistentData().getFloat("SbwAcceleration") : 0.005f;
                float brk = target.getPersistentData().contains("SbwBraking") ? target.getPersistentData().getFloat("SbwBraking") : 0.02f;
                float tr = target.getPersistentData().contains("SbwTurnRadius") ? target.getPersistentData().getFloat("SbwTurnRadius") : 1.0f;
                boolean phys = target.getPersistentData().getBoolean("SbwPhysicsEnabled");

                com.agent.sbwnpcaddon.network.SbwNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) event.getEntity()),
                    new com.agent.sbwnpcaddon.network.SyncVehicleConfigPacket(target.getId(), type, ms, acc, brk, tr, phys)
                );
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getItemStack().getItem() == ItemRegistry.VEHICLE_CONFIG_TOOL.get()) {
            if (!event.getLevel().isClientSide && event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target) {
                int type = target.getPersistentData().getInt("SbwVehicleType");
                float ms = target.getPersistentData().contains("SbwMaxSpeed") ? target.getPersistentData().getFloat("SbwMaxSpeed") : 0.5f;
                float acc = target.getPersistentData().contains("SbwAcceleration") ? target.getPersistentData().getFloat("SbwAcceleration") : 0.005f;
                float brk = target.getPersistentData().contains("SbwBraking") ? target.getPersistentData().getFloat("SbwBraking") : 0.02f;
                float tr = target.getPersistentData().contains("SbwTurnRadius") ? target.getPersistentData().getFloat("SbwTurnRadius") : 1.0f;
                boolean phys = target.getPersistentData().getBoolean("SbwPhysicsEnabled");

                com.agent.sbwnpcaddon.network.SbwNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) event.getEntity()),
                    new com.agent.sbwnpcaddon.network.SyncVehicleConfigPacket(target.getId(), type, ms, acc, brk, tr, phys)
                );
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    
    private void onLivingTick(LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();
        if (entity.getPersistentData().getBoolean("SbwPhysicsEnabled") && entity instanceof net.minecraft.world.entity.Mob mob) {
            
            // Safely inject VehicleMoveControl and LookControl if not present to neutralize vanilla AI
            if (!(mob.getMoveControl() instanceof com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl)) {
                ((com.agent.sbwnpcaddon.mixin.MobAccessor)mob).setMoveControl(new com.agent.sbwnpcaddon.entity.physics.VehicleMoveControl(mob));
            }
            if (!(mob.getLookControl() instanceof com.agent.sbwnpcaddon.entity.physics.VehicleLookControl)) {
                ((com.agent.sbwnpcaddon.mixin.MobAccessor)mob).setLookControl(new com.agent.sbwnpcaddon.entity.physics.VehicleLookControl(mob));
            }

            var module = VehicleConfigTool.physicsModules.get(mob);
            if (module == null) {
                module = new com.agent.sbwnpcaddon.entity.physics.SbwPhysicsModule(mob);
                VehicleConfigTool.physicsModules.put(mob, module);
            }
            module.tick(); 
        }
    }
}
