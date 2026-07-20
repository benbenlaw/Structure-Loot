package com.benbenlaw.structureloot.data;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.loot.StructureLootTokenModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class SLLootModifierProvider extends GlobalLootModifierProvider {

    public SLLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, StructureLoot.MOD_ID);
    }

    @Override
    protected void start() {

        add("structure_loot_token", new StructureLootTokenModifier(
                new LootItemCondition[]{},
                1001,
                0.1
        ));
    }
}