package com.benbenlaw.structureloot.data;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.SLBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SLBlockTags extends BlockTagsProvider {

    SLBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, StructureLoot.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(SLBlocks.STRUCTURE_LOOT_BLOCK.get())
                .add(SLBlocks.STRUCTURE_LOOT_PART.get())
        ;
    }

}
