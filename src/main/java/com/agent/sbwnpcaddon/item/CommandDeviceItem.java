package com.agent.sbwnpcaddon.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;

public class CommandDeviceItem extends Item {
    public CommandDeviceItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            java.util.List<String> names = new java.util.ArrayList<>();
            
            for (net.minecraft.world.entity.Entity e : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(64.0))) {
                if (e.getClass().getName().equals("noppes.npcs.entity.EntityCustomNpc")) {
                    try {
                        java.lang.reflect.Method getOwner = e.getClass().getMethod("getOwner");
                        Object owner = getOwner.invoke(e);
                        if (owner == player) {
                            java.lang.reflect.Method isFollower = e.getClass().getMethod("isFollower");
                            boolean following = (boolean) isFollower.invoke(e);
                            if (following) {
                                ids.add(e.getId());
                                names.add(e.getDisplayName().getString());
                            }
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            }
            
            com.agent.sbwnpcaddon.network.SbwNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player),
                new com.agent.sbwnpcaddon.network.OpenCommandDeviceGuiPacket(ids, names)
            );
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
