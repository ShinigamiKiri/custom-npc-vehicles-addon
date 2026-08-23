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
            java.util.List<Integer> presets = new java.util.ArrayList<>();
            java.util.List<Boolean> isCommandActive = new java.util.ArrayList<>();
            java.util.List<Integer> activeModes = new java.util.ArrayList<>();
            java.util.List<Double> targetXs = new java.util.ArrayList<>();
            java.util.List<Double> targetYs = new java.util.ArrayList<>();
            java.util.List<Double> targetZs = new java.util.ArrayList<>();
            java.util.List<Double> targetX2s = new java.util.ArrayList<>();
            java.util.List<Double> targetY2s = new java.util.ArrayList<>();
            java.util.List<Double> targetZ2s = new java.util.ArrayList<>();
            
            for (net.minecraft.world.entity.Entity e : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(64.0))) {
                try {
                    java.lang.reflect.Method getOwner = e.getClass().getMethod("getOwner");
                    Object owner = getOwner.invoke(e);
                    if (owner instanceof net.minecraft.world.entity.Entity && ((net.minecraft.world.entity.Entity) owner).getUUID().equals(player.getUUID())) {
                        java.lang.reflect.Method isFollower = e.getClass().getMethod("isFollower");
                        boolean following = (boolean) isFollower.invoke(e);
                        if (following) {
                            ids.add(e.getId());
                            names.add(e.getDisplayName().getString());
                            net.minecraft.nbt.CompoundTag data = e.getPersistentData();
                            int preset = data.contains("SbwCombatPreset") ? data.getInt("SbwCombatPreset") : 1;
                            presets.add(preset);
                            
                            isCommandActive.add(data.getBoolean("SbwCommandActive"));
                            int mode = data.contains("SbwCommandMode") ? data.getInt("SbwCommandMode") : (data.getBoolean("SbwCommandPatrol") ? 3 : 2);
                            activeModes.add(mode);
                            
                            targetXs.add(data.getDouble("SbwCmdX1"));
                            targetYs.add(data.getDouble("SbwCmdY1"));
                            targetZs.add(data.getDouble("SbwCmdZ1"));
                            targetX2s.add(data.getDouble("SbwCmdX2"));
                            targetY2s.add(data.getDouble("SbwCmdY2"));
                            targetZ2s.add(data.getDouble("SbwCmdZ2"));
                        }
                    }
                } catch (Exception ex) {
                    // ignore
                }
            }
            
            com.agent.sbwnpcaddon.network.SbwNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player),
                new com.agent.sbwnpcaddon.network.OpenCommandDeviceGuiPacket(ids, names, presets, isCommandActive, activeModes, targetXs, targetYs, targetZs, targetX2s, targetY2s, targetZ2s)
            );
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
