package com.benbenlaw.structureloot.integration;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.event.client.ClientLootPreviewCache;
import com.benbenlaw.structureloot.event.client.ClientRecipeCache;
import com.benbenlaw.structureloot.item.SLDataComponents;
import com.benbenlaw.structureloot.item.SLItems;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEISLPlugin implements IModPlugin {

    private IRecipeManager recipeManager;
    private List<LootGeneratorCategory.Entry> registeredEntries = List.of();

    @Override
    public Identifier getPluginUid() {
        return StructureLoot.identifier("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(SLItems.BLOCK_TOKEN.get(), new ItemSubtypeInterpreter());
        registration.registerSubtypeInterpreter(SLItems.ENTITY_TOKEN.get(), new ItemSubtypeInterpreter());
        registration.registerSubtypeInterpreter(SLItems.STRUCTURE_TOKEN.get(), new ItemSubtypeInterpreter());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new LootGeneratorCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registeredEntries = buildEntries();
        registration.addRecipes(LootGeneratorCategory.TYPE, registeredEntries);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(LootGeneratorCategory.TYPE, SLItems.STRUCTURE_LOOT_BLOCK.get());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        recipeManager = jeiRuntime.getRecipeManager();
        // Loot previews can arrive after JEI has started, so swap the entries in when they do
        ClientLootPreviewCache.setListener(this::refreshEntries);
    }

    @Override
    public void onRuntimeUnavailable() {
        recipeManager = null;
        ClientLootPreviewCache.setListener(() -> {});
    }

    private void refreshEntries() {
        if (recipeManager == null) return;

        List<LootGeneratorCategory.Entry> newEntries = buildEntries();
        recipeManager.hideRecipes(LootGeneratorCategory.TYPE, registeredEntries);
        recipeManager.addRecipes(LootGeneratorCategory.TYPE, newEntries);
        registeredEntries = newEntries;
    }

    private static List<LootGeneratorCategory.Entry> buildEntries() {
        List<LootGeneratorCategory.Entry> entries = new ArrayList<>();

        for (StructureLootRecipe recipe : ClientRecipeCache.getCachedStructureLootRecipes()) {
            if (recipe.lootTables().isEmpty()) continue;

            ItemStack token = new ItemStack(switch (recipe.lootTables().getFirst().type()) {
                case GENERIC -> SLItems.STRUCTURE_TOKEN.get();
                case BLOCK -> SLItems.BLOCK_TOKEN.get();
                case ENTITY -> SLItems.ENTITY_TOKEN.get();
            });
            token.set(SLDataComponents.LOOT_ID.get(), recipe.lootId());
            token.set(DataComponents.MAX_DAMAGE, recipe.maxDurability());

            entries.add(new LootGeneratorCategory.Entry(token, recipe, ClientLootPreviewCache.get(recipe.lootId())));
        }
        return entries;
    }
}
