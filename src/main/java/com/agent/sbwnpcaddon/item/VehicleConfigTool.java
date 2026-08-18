package com.agent.sbwnpcaddon.item;

import com.agent.sbwnpcaddon.entity.physics.SbwPhysicsModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.WeakHashMap;

public class VehicleConfigTool extends Item {
    
    // Simple cache to store the module per entity without needing complex Capabilities
    public static final WeakHashMap<LivingEntity, SbwPhysicsModule> physicsModules = new WeakHashMap<>();

    public VehicleConfigTool(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!player.level().isClientSide && interactionTarget instanceof Mob) {
            Mob mob = (Mob) interactionTarget;
            boolean isEnabled = mob.getPersistentData().getBoolean("SbwPhysicsEnabled");
            
            if (isEnabled) {
                mob.getPersistentData().putBoolean("SbwPhysicsEnabled", false);
                physicsModules.remove(mob);
                player.displayClientMessage(Component.literal("Vehicle Physics Disabled for " + mob.getName().getString()), true);
            } else {
                mob.getPersistentData().putBoolean("SbwPhysicsEnabled", true);
                physicsModules.put(mob, new SbwPhysicsModule(mob));
                player.displayClientMessage(Component.literal("Vehicle Physics Enabled for " + mob.getName().getString()), true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
