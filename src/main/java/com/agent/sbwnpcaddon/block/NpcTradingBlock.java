package com.agent.sbwnpcaddon.block;

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
