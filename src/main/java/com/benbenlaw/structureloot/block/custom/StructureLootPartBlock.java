package com.benbenlaw.structureloot.block.custom;

import com.benbenlaw.structureloot.block.entity.StructureLootBlockEntity;
import com.benbenlaw.structureloot.block.entity.StructureLootPartBlockEntity;
import com.benbenlaw.structureloot.item.SLItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class StructureLootPartBlock extends BaseEntityBlock {

    public static final MapCodec<StructureLootPartBlock> CODEC = simpleCodec(StructureLootPartBlock::new);

    @Override
    public @NonNull MapCodec<StructureLootPartBlock> codec() {
        return CODEC;
    }

    public StructureLootPartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof StructureLootPartBlockEntity part) {
                BlockPos controllerPos = part.getControllerPos();
                if (level.getBlockEntity(controllerPos) instanceof StructureLootBlockEntity controller) {
                    player.openMenu(new SimpleMenuProvider(controller, controller.getDisplayName()), controllerPos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        return new ItemStack(SLItems.STRUCTURE_LOOT_BLOCK.get());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new StructureLootPartBlockEntity(pos, state);
    }
}