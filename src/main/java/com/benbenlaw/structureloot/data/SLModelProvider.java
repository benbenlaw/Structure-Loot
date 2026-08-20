package com.benbenlaw.structureloot.data;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.SLBlocks;
import com.benbenlaw.structureloot.block.entity.renderer.StructureLootSpecialRenderer;
import com.benbenlaw.structureloot.item.SLItems;
import com.mojang.math.Transformation;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.stream.Stream;

public class SLModelProvider extends ModelProvider {

    public SLModelProvider(PackOutput output) {
        super(output, StructureLoot.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(SLItems.TOKEN_CHARM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(SLItems.STRUCTURE_TOKEN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(SLItems.BLOCK_TOKEN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(SLItems.ENTITY_TOKEN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(SLItems.STRUCTURE_LOOT_BLOCK.get(), ModelTemplates.FLAT_ITEM);

        createStructureLootItemModel(itemModels, SLBlocks.STRUCTURE_LOOT_BLOCK.get());
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.empty();
    }

    public void createStructureLootItemModel(ItemModelGenerators itemModels, Block block) {
        SpecialModelRenderer.Unbaked special = new StructureLootSpecialRenderer.Unbaked();

        Transformation transform = new Transformation(
                new Vector3f(0.0F, 0.0F, 0.0F),
                new Quaternionf(),
                new Vector3f(0.375F, 0.375F, 0.375F),
                new Quaternionf()
        );
    }
}
