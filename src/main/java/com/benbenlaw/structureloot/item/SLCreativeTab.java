package com.benbenlaw.structureloot.item;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.SLBlocks;
import com.benbenlaw.structureloot.event.client.ClientRecipeCache;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
                                var recipes = ClientRecipeCache.getCachedStructureLootRecipes();
                                addTokensForType(recipes, StructureLootRecipe.LootContextType.GENERIC, output);
                                addTokensForType(recipes, StructureLootRecipe.LootContextType.BLOCK, output);
                                addTokensForType(recipes, StructureLootRecipe.LootContextType.ENTITY, output);
                            })
                            .build());

    private static Item itemForType(StructureLootRecipe.LootContextType type) {
        return switch (type) {
            case BLOCK -> SLItems.BLOCK_TOKEN.get();
            case ENTITY -> SLItems.ENTITY_TOKEN.get();
            case GENERIC -> SLItems.STRUCTURE_TOKEN.get();
        };
    }

    private static void addTokensForType(
            Iterable<StructureLootRecipe> recipes,
            StructureLootRecipe.LootContextType type,
            CreativeModeTab.Output output) {

        for (StructureLootRecipe recipe : recipes) {
            boolean hasType = recipe.lootTables().stream()
                    .anyMatch(roll -> roll.type() == type);

            if (hasType) {
                ItemStack token = new ItemStack(itemForType(type));
                token.set(SLDataComponents.LOOT_ID.get(), recipe.lootId());
                token.set(DataComponents.MAX_DAMAGE, recipe.maxDurability());
                output.accept(token);
            }
        }
    }

}
