package com.benbenlaw.structureloot.block.entity.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class StructureLootBlockEntityRenderState extends BlockEntityRenderState {
    public float spinAngle;
    public float chestOpenness;
    public float itemFallProgress;
    public boolean showFallingItem;
    public ItemStack previewItem;
}