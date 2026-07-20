package com.benbenlaw.structureloot.block;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.entity.StructureLootBlockEntity;
import com.benbenlaw.structureloot.block.entity.StructureLootPartBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SLBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, StructureLoot.MOD_ID);

    public static final Supplier<BlockEntityType<StructureLootBlockEntity>> STRUCTURE_LOOT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("structure_loot_block_entity", () ->
                    new BlockEntityType<>(StructureLootBlockEntity::new, SLBlocks.STRUCTURE_LOOT_BLOCK.get()));

    public static final Supplier<BlockEntityType<StructureLootPartBlockEntity>> STRUCTURE_LOOT_PART_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("structure_loot_part_block_entity", () ->
                    new BlockEntityType<>(StructureLootPartBlockEntity::new, SLBlocks.STRUCTURE_LOOT_PART.get()));

}
