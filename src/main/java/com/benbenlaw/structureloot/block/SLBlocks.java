package com.benbenlaw.structureloot.block;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.custom.StructureLootBlock;
import com.benbenlaw.structureloot.block.custom.StructureLootPartBlock;
import com.benbenlaw.structureloot.item.SLItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class SLBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StructureLoot.MOD_ID);

    public static final DeferredBlock<Block> STRUCTURE_LOOT_BLOCK = BLOCKS.registerBlock("structure_loot_block",
            properties -> new StructureLootBlock(properties
                    .strength(1.0F)
                    .noOcclusion()));

    public static final DeferredBlock<Block> STRUCTURE_LOOT_PART = registerBlock("structure_loot_part",
            properties -> new StructureLootPartBlock(properties
                    .strength(1.0F)
                    .noOcclusion()));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        SLItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }
}
