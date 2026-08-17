package com.benbenlaw.structureloot.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.structureloot.block.SLBlockEntities;
import com.benbenlaw.structureloot.util.EnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

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

    private @Nullable StructureLootBlockEntity getController() {
        if (level == null || controllerPos.equals(BlockPos.ZERO)) return null;
        if (level.getBlockEntity(controllerPos) instanceof StructureLootBlockEntity controller) {
            return controller;
        }
        return null;
    }

    public @Nullable ItemStacksResourceHandler getItemHandler() {
        StructureLootBlockEntity controller = getController();
        return controller != null ? controller.getItemHandler() : null;
    }

    public @Nullable EnergyHandler getEnergyHandler() {
        StructureLootBlockEntity controller = getController();
        return controller != null ? controller.getEnergyHandler() : null;
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
        StructureLootBlockEntity controller = getController();
        if (level != null && !level.isClientSide() && controller != null) {
            controller.breakStructure();
        }
    }
}