package com.benbenlaw.structureloot.item;

import com.benbenlaw.structureloot.StructureLoot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SLItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StructureLoot.MOD_ID);

    public static final DeferredItem<Item> STRUCTURE_TOKEN = ITEMS.registerItem("structure_token",
            Item::new, properties -> properties.durability(10));

    public static final DeferredItem<StructureLootBlockItem> STRUCTURE_LOOT_BLOCK =
            ITEMS.registerItem("structure_loot_block",
                    (properties) -> new StructureLootBlockItem(properties));

}
