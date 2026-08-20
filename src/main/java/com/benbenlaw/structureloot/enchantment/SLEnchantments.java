package com.benbenlaw.structureloot.enchantment;

import com.benbenlaw.structureloot.StructureLoot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class SLEnchantments {

    public static final ResourceKey<Enchantment> LOOT_ATTUNED = key("loot_attuned");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, StructureLoot.identifier(name));
    }
}