package com.benbenlaw.structureloot.item;

import com.benbenlaw.structureloot.block.SLBlocks;
import com.benbenlaw.structureloot.block.entity.StructureLootPartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class StructureLootBlockItem extends BlockItem {

    public StructureLootBlockItem(Properties properties) {
        super(SLBlocks.STRUCTURE_LOOT_BLOCK.get(), properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();

        BlockState clickedState = level.getBlockState(clickedPos);
        BlockPos basePos = clickedState.canBeReplaced() ? clickedPos : clickedPos.relative(face);
        BlockPos controllerPos = basePos.relative(face);

        for (BlockPos checkPos : BlockPos.betweenClosed(
                controllerPos.offset(-1, -1, -1), controllerPos.offset(1, 1, 1))) {
            if (!level.getBlockState(checkPos).canBeReplaced()) {
                return InteractionResult.FAIL;
            }
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockState controllerState = getBlock().defaultBlockState();
        level.setBlock(controllerPos, controllerState, 3);

        for (BlockPos partPos : BlockPos.betweenClosed(
                controllerPos.offset(-1, -1, -1), controllerPos.offset(1, 1, 1))) {
            if (partPos.equals(controllerPos)) continue;
            level.setBlock(partPos, SLBlocks.STRUCTURE_LOOT_PART.get().defaultBlockState(), 3);
            if (level.getBlockEntity(partPos) instanceof StructureLootPartBlockEntity part) {
                part.setControllerPos(controllerPos);
            }
        }

        if (player != null && !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}