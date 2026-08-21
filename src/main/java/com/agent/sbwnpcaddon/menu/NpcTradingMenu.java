package com.agent.sbwnpcaddon.menu;

import com.agent.sbwnpcaddon.block.entity.NpcTradingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import noppes.npcs.items.ItemSoulstoneFilled;

public class NpcTradingMenu extends AbstractContainerMenu {
    public final NpcTradingBlockEntity blockEntity;
    public final Player player;
    
    // We create a dummy inventory for the trade input
    public final net.minecraftforge.items.ItemStackHandler tradeInput = new net.minecraftforge.items.ItemStackHandler(9);

    public NpcTradingMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public NpcTradingMenu(int id, Inventory inv, BlockEntity entity) {
        super(MenuRegistry.NPC_TRADING_MENU.get(), id);
        this.player = inv.player;
        this.blockEntity = (NpcTradingBlockEntity) entity;

        // Trade Input Grid (9 slots) [0-8]
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new SlotItemHandler(tradeInput, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }
        
        // Output Slot (Display Only) [9]
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 9, 124, 35) {
            @Override
            public boolean mayPickup(Player playerIn) {
                return false;
            }
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Requirement Slots (Display Only, placed far away or just overlayed) [10-18]
        // We put them at negative coordinates so they don't interfere with clicks, but are synced.
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), i, -1000, -1000) {
                @Override
                public boolean mayPickup(Player playerIn) { return false; }
                @Override
                public boolean mayPlace(ItemStack stack) { return false; }
            });
        }

        // Player Inventory [19-45]
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Player Hotbar [46-54]
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.clearContainer(playerIn, new net.minecraft.world.SimpleContainer(
            tradeInput.getStackInSlot(0), tradeInput.getStackInSlot(1), tradeInput.getStackInSlot(2),
            tradeInput.getStackInSlot(3), tradeInput.getStackInSlot(4), tradeInput.getStackInSlot(5),
            tradeInput.getStackInSlot(6), tradeInput.getStackInSlot(7), tradeInput.getStackInSlot(8)
        ));
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            // Skip display slots
            if (index == 9 || (index >= 10 && index <= 18)) {
                return ItemStack.EMPTY;
            }

            if (index < 9) { // From trade input
                if (!this.moveItemStackTo(itemstack1, 19, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else { // From player inventory
                if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    public void tryCompleteTrade() {
        if (player.level().isClientSide) return;
        
        var required = blockEntity.getItemHandler();
        
        boolean matches = true;
        for (int i = 0; i < 9; i++) {
            ItemStack req = required.getStackInSlot(i);
            ItemStack prov = tradeInput.getStackInSlot(i);
            if (req.isEmpty() && prov.isEmpty()) continue;
            if (req.isEmpty() != prov.isEmpty()) { matches = false; break; }
            if (req.getItem() != prov.getItem() || req.getCount() > prov.getCount()) { matches = false; break; }
            if (req.hasTag() && !ItemStack.isSameItemSameTags(req, prov)) { matches = false; break; }
        }
        
        if (matches) {
            // Consume items
            for (int i = 0; i < 9; i++) {
                ItemStack req = required.getStackInSlot(i);
                if (!req.isEmpty()) {
                    tradeInput.extractItem(i, req.getCount(), false);
                }
            }
            
            // Give NPC
            ItemStack result = required.getStackInSlot(9);
            if (!result.isEmpty() && result.getItem() instanceof ItemSoulstoneFilled) {
                BlockPos pos = blockEntity.getBlockPos().above();
                ItemSoulstoneFilled.Spawn(player, result, player.level(), pos);
            } else {
                // If not an NPC, just give the item (fallback)
                if (!result.isEmpty()) {
                    net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                        player.level(), player.getX(), player.getY(), player.getZ(), result.copy()
                    );
                    player.level().addFreshEntity(entity);
                }
            }
        }
    }
}
