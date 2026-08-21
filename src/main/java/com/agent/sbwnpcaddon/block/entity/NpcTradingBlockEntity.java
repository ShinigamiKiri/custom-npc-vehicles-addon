package com.agent.sbwnpcaddon.block.entity;

import com.agent.sbwnpcaddon.menu.NpcTradingMenu;
import com.agent.sbwnpcaddon.menu.NpcTradingSetupMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NpcTradingBlockEntity extends BlockEntity {
    private final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    
    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    public NpcTradingBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.NPC_TRADING_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public MenuProvider getSetupMenuProvider() {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Setup NPC Trade (Creative)");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return new NpcTradingSetupMenu(id, inv, NpcTradingBlockEntity.this);
            }
        };
    }

    public MenuProvider getTradeMenuProvider() {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("NPC Trade");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return new NpcTradingMenu(id, inv, NpcTradingBlockEntity.this);
            }
        };
    }
}
