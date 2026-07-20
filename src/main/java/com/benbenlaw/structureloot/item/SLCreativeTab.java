package com.benbenlaw.structureloot.item;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.SLBlocks;
import com.benbenlaw.structureloot.event.client.ClientRecipeCache;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SLCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StructureLoot.MOD_ID);

    public static final Supplier<CreativeModeTab> STRUCTURE_LOOT_TAB =
            CREATIVE_MODE_TABS.register(StructureLoot.MOD_ID, () ->
                    CreativeModeTab.builder()
                            .withTabsBefore(CreativeModeTabs.COMBAT)
                            .icon(() -> SLBlocks.STRUCTURE_LOOT_BLOCK.get()
                                    .asItem()
                                    .getDefaultInstance())
                            .title(Component.translatable("itemGroup." + StructureLoot.MOD_ID))

                            .displayItems((parameters, output) -> {
                                output.accept(SLBlocks.STRUCTURE_LOOT_BLOCK.get());

                                ClientRecipeCache.getCachedStructureLootRecipes().forEach(recipe -> {
                                    ItemStack token = new ItemStack(SLItems.STRUCTURE_TOKEN.get());
                                    token.set(SLDataComponents.STRUCTURE_ID.get(), recipe.structure());
                                    output.accept(token);
                                });
                            })
                            .build());



}
