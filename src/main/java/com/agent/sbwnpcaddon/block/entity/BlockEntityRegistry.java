package com.agent.sbwnpcaddon.block.entity;

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
