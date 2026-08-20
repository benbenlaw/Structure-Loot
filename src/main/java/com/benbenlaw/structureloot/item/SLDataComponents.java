package com.benbenlaw.structureloot.item;

import com.benbenlaw.structureloot.StructureLoot;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SLDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, StructureLoot.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> LOOT_ID =
            COMPONENTS.register("loot_id", () ->
                    DataComponentType.<Identifier>builder()
                            .persistent(Identifier.CODEC)
                            .networkSynchronized(Identifier.STREAM_CODEC)
                            .cacheEncoding()
                            .build());

}
