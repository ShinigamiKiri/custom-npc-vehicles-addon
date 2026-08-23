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
        com.agent.sbwnpcaddon.block.BlockRegistry.register(modEventBus);
        com.agent.sbwnpcaddon.block.entity.BlockEntityRegistry.register(modEventBus);
        com.agent.sbwnpcaddon.menu.MenuRegistry.register(modEventBus);
        
        MinecraftForge.EVENT_BUS.addListener(this::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingHurt);
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
                int am = target.getPersistentData().contains("SbwAircraftMode") ? target.getPersistentData().getInt("SbwAircraftMode") : ((type == 3) ? 0 : 1);
                boolean phys = target.getPersistentData().getBoolean("SbwPhysicsEnabled");

                com.agent.sbwnpcaddon.network.SbwNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) event.getEntity()),
                    new com.agent.sbwnpcaddon.network.SyncVehicleConfigPacket(target.getId(), type, ms, acc, brk, tr, am, phys)
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
                int am = target.getPersistentData().contains("SbwAircraftMode") ? target.getPersistentData().getInt("SbwAircraftMode") : ((type == 3) ? 0 : 1);
                boolean phys = target.getPersistentData().getBoolean("SbwPhysicsEnabled");

                com.agent.sbwnpcaddon.network.SbwNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) event.getEntity()),
                    new com.agent.sbwnpcaddon.network.SyncVehicleConfigPacket(target.getId(), type, ms, acc, brk, tr, am, phys)
                );
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
    
    private void onLivingTick(LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            com.agent.sbwnpcaddon.entity.ai.CommandDeviceHelper.ensureCommandRestored(mob);
            com.agent.sbwnpcaddon.world.ChunkLoadManager.maintainChunkLoading(mob);
        }

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

    private void onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        var entity = event.getEntity();
        if (!entity.level().isClientSide && entity instanceof net.minecraft.world.entity.Mob mob) {
            if (mob.getPersistentData().getBoolean("SbwCommandActive")) {
                int mode = mob.getPersistentData().getInt("SbwCommandMode");
                if (mode == 0 || mode == 4 || mode == 5) {
                    var sourceEntity = event.getSource().getEntity();
                    if (sourceEntity instanceof net.minecraft.world.entity.LivingEntity attacker) {
                        // Priority 1: Self-defense. If attacked, prioritize the attacker.
                        mob.setTarget(attacker);
                        mob.getPersistentData().putBoolean("SbwPrioritizeSelfDefense", true);
                        mob.getLookControl().setLookAt(attacker, 30.0F, 30.0F);
                        if (mob.distanceToSqr(attacker) < 16.0) {
                            mob.doHurtTarget(attacker);
                        } else if (mob instanceof net.minecraft.world.entity.monster.RangedAttackMob ranged) {
                            ranged.performRangedAttack(attacker, 1.0f);
                        }
                    }
                }
            }
        }

        // Owner Assist: Follow/Guard/Patrol-Guard mode NPCs assist owner's attacks
        var trueSource = event.getSource().getEntity();
        if (trueSource instanceof net.minecraft.world.entity.player.Player player && !entity.level().isClientSide) {
            if (entity.isAlive()) {
                double assistRadius = 48.0; 
                java.util.List<net.minecraft.world.entity.Mob> nearbyMobs = player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.Mob.class, 
                    player.getBoundingBox().inflate(assistRadius)
                );

                for (net.minecraft.world.entity.Mob assistMob : nearbyMobs) {
                    if (assistMob.getPersistentData().getBoolean("SbwCommandActive")) {
                        int mode = assistMob.getPersistentData().getInt("SbwCommandMode");
                        if (mode == 0 || mode == 4 || mode == 5) { // Follow, Guard, Patrol-Guard
                            try {
                                java.lang.reflect.Method getOwner = assistMob.getClass().getMethod("getOwner");
                                Object owner = getOwner.invoke(assistMob);
                                if (owner instanceof net.minecraft.world.entity.Entity o && o.getUUID().equals(player.getUUID())) {
                                    if (!assistMob.isAlliedTo(entity) && assistMob.canAttack(entity) && entity != assistMob && entity != player) {
                                        // Self-defense check
                                        boolean selfDefense = assistMob.getPersistentData().getBoolean("SbwPrioritizeSelfDefense");
                                        net.minecraft.world.entity.LivingEntity currentTarget = assistMob.getTarget();
                                        if (selfDefense && currentTarget != null && currentTarget.isAlive() && currentTarget != entity) {
                                            // Do not override if currently prioritizing self-defense
                                            continue;
                                        }
                                        assistMob.getPersistentData().putBoolean("SbwPrioritizeSelfDefense", false);
                                        assistMob.setTarget(entity);
                                        assistMob.getPersistentData().putBoolean("SbwForceOwnerAssist", true);
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        }
    }
}
