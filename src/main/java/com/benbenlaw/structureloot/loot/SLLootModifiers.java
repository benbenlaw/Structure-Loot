package com.benbenlaw.structureloot.loot;

import com.benbenlaw.structureloot.StructureLoot;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class SLLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, StructureLoot.MOD_ID);

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> STRUCTURE_LOOT_TOKEN =
            LOOT_MODIFIER_SERIALIZERS.register("structure_loot_token", () -> StructureLootTokenModifier.CODEC);
}
