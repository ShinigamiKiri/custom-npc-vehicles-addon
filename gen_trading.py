import os
import shutil

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

write_file("src/main/java/com/agent/sbwnpcaddon/block/BlockRegistry.java", """package com.agent.sbwnpcaddon.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.agent.sbwnpcaddon.item.ItemRegistry;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "sbw_npc_addon");

    public static final RegistryObject<Block> NPC_TRADING_BLOCK = BLOCKS.register("npc_trading_block",
            () -> new NpcTradingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f, 6.0f).requiresCorrectToolForDrops()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
""")

write_file("src/main/java/com/agent/sbwnpcaddon/block/NpcTradingBlock.java", """package com.agent.sbwnpcaddon.block;

import com.agent.sbwnpcaddon.block.entity.BlockEntityRegistry;
import com.agent.sbwnpcaddon.block.entity.NpcTradingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class NpcTradingBlock extends BaseEntityBlock {
    public NpcTradingBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.NPC_TRADING_BLOCK_ENTITY.get().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof NpcTradingBlockEntity tradingEntity) {
                if (player.isCreative() || player.hasPermissions(2)) {
                    NetworkHooks.openScreen((ServerPlayer) player, tradingEntity.getSetupMenuProvider(), pos);
                } else {
                    NetworkHooks.openScreen((ServerPlayer) player, tradingEntity.getTradeMenuProvider(), pos);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
""")

write_file("src/main/java/com/agent/sbwnpcaddon/block/entity/BlockEntityRegistry.java", """package com.agent.sbwnpcaddon.block.entity;

import com.agent.sbwnpcaddon.block.BlockRegistry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "sbw_npc_addon");

    public static final RegistryObject<BlockEntityType<NpcTradingBlockEntity>> NPC_TRADING_BLOCK_ENTITY = BLOCK_ENTITIES.register("npc_trading_block_entity",
            () -> BlockEntityType.Builder.of(NpcTradingBlockEntity::new, BlockRegistry.NPC_TRADING_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
""")

write_file("src/main/java/com/agent/sbwnpcaddon/block/entity/NpcTradingBlockEntity.java", """package com.agent.sbwnpcaddon.block.entity;

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
""")

write_file("src/main/java/com/agent/sbwnpcaddon/menu/MenuRegistry.java", """package com.agent.sbwnpcaddon.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "sbw_npc_addon");

    public static final RegistryObject<MenuType<NpcTradingSetupMenu>> NPC_TRADING_SETUP_MENU = MENUS.register("npc_trading_setup_menu",
            () -> IForgeMenuType.create(NpcTradingSetupMenu::new));
            
    public static final RegistryObject<MenuType<NpcTradingMenu>> NPC_TRADING_MENU = MENUS.register("npc_trading_menu",
            () -> IForgeMenuType.create(NpcTradingMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
""")

write_file("src/main/java/com/agent/sbwnpcaddon/menu/NpcTradingSetupMenu.java", """package com.agent.sbwnpcaddon.menu;

import com.agent.sbwnpcaddon.block.entity.NpcTradingBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class NpcTradingSetupMenu extends AbstractContainerMenu {
    private final NpcTradingBlockEntity blockEntity;
    private final Player player;

    public NpcTradingSetupMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public NpcTradingSetupMenu(int id, Inventory inv, BlockEntity entity) {
        super(MenuRegistry.NPC_TRADING_SETUP_MENU.get(), id);
        this.player = inv.player;
        this.blockEntity = (NpcTradingBlockEntity) entity;

        var itemHandler = blockEntity.getItemHandler();
        
        // Setup Grid (9 slots)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new SlotItemHandler(itemHandler, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }
        
        // Output Slot (1 slot)
        this.addSlot(new SlotItemHandler(itemHandler, 9, 124, 35));

        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Player Hotbar
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 10) {
                if (!this.moveItemStackTo(itemstack1, 10, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 10, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}
""")

