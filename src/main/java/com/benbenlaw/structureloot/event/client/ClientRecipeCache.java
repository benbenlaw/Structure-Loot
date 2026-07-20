package com.benbenlaw.structureloot.event.client;

import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClientRecipeCache {

    public static Map<Identifier, StructureLootRecipe> cachedStructureLootRecipes = new HashMap<>();

    public static void setCachedStructureLootRecipes(Map<Identifier, StructureLootRecipe> cachedStructureLootRecipes) {
        ClientRecipeCache.cachedStructureLootRecipes = cachedStructureLootRecipes;
    }

    public static Collection<StructureLootRecipe> getCachedStructureLootRecipes() {
        return cachedStructureLootRecipes.values();
    }

}
