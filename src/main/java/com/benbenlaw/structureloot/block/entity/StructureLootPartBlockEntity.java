package com.benbenlaw.structureloot.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.structureloot.block.SLBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class StructureLootPartBlockEntity extends SyncableBlockEntity {

    private BlockPos controllerPos = BlockPos.ZERO;

    public StructureLootPartBlockEntity(BlockPos pos, BlockState state) {
        super(SLBlockEntities.STRUCTURE_LOOT_PART_BLOCK_ENTITY.get(), pos, state);
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos pos) {
        this.controllerPos = pos;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    @Override
    protected void saveAdditional(ValueOutput output) {
        output.putInt("ControllerX", controllerPos.getX());
        output.putInt("ControllerY", controllerPos.getY());
        output.putInt("ControllerZ", controllerPos.getZ());
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        int x = input.getIntOr("ControllerX", 0);
        int y = input.getIntOr("ControllerY", 0);
        int z = input.getIntOr("ControllerZ", 0);
        controllerPos = new BlockPos(x, y, z);
        super.loadAdditional(input);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()
                && level.getBlockEntity(controllerPos) instanceof StructureLootBlockEntity controller) {
            controller.breakStructure();
        }
    }
}