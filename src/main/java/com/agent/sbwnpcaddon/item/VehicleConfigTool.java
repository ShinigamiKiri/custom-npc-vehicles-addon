package com.agent.sbwnpcaddon.item;

import com.agent.sbwnpcaddon.client.screen.VehicleConfigScreen;
import com.agent.sbwnpcaddon.entity.physics.SbwPhysicsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.WeakHashMap;

public class VehicleConfigTool extends Item {
    
    public static final WeakHashMap<LivingEntity, SbwPhysicsModule> physicsModules = new WeakHashMap<>();

    public VehicleConfigTool(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.level().isClientSide) {
            Minecraft.getInstance().setScreen(new VehicleConfigScreen(interactionTarget));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
