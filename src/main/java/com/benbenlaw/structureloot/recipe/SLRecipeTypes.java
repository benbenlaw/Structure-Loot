package com.benbenlaw.structureloot.recipe;

import com.benbenlaw.structureloot.StructureLoot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SLRecipeTypes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZER =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, StructureLoot.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, StructureLoot.MOD_ID);

    //Structure Loot Recipe
    public static final Supplier<RecipeSerializer<StructureLootRecipe>> ENTITY_MELTING_SERIALIZER =
            SERIALIZER.register("structure_loot", () -> StructureLootRecipe.SERIALIZER);
    public static final Supplier<RecipeType<StructureLootRecipe>> ENTITY_MELTING_TYPE =
            TYPES.register("structure_loot", () -> StructureLootRecipe.TYPE);

}
