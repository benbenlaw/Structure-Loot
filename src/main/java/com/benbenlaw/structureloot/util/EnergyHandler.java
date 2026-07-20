package com.benbenlaw.structureloot.util;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

public class EnergyHandler extends SimpleEnergyHandler {
    private final SyncableBlockEntity blockEntity;

    public EnergyHandler(int capacity, int maxTransfer, SyncableBlockEntity blockEntity) {
        super(capacity, maxTransfer);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        blockEntity.sync();
        super.onEnergyChanged(previousAmount);
    }
}