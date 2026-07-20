package com.benbenlaw.structureloot.data;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.item.SLItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

public class SLModelProvider extends ModelProvider {

    public SLModelProvider(PackOutput output) {
        super(output, StructureLoot.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        //Items
        itemModels.generateFlatItem(SLItems.STRUCTURE_TOKEN.get(), ModelTemplates.FLAT_ITEM);

    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }
}
