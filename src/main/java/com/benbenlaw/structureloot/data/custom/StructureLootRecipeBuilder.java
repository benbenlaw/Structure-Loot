package com.benbenlaw.structureloot.data.custom;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructureLootRecipeBuilder implements RecipeBuilder {

    protected String group;
    protected Identifier structure;
    protected List<Identifier> lootTables;
    protected int rolls;
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    public StructureLootRecipeBuilder(Identifier structure, List<Identifier> lootTables, int rolls) {
        this.structure = structure;
        this.lootTables = lootTables;
        this.rolls = rolls;
    }

    public static StructureLootRecipeBuilder structureLootRecipe(Identifier structure, List<Identifier> lootTables, int rolls) {
        return new StructureLootRecipeBuilder(structure, lootTables, rolls);
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull RecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        Identifier structure = this.structure;
        return ResourceKey.create(
                Registries.RECIPE,
                StructureLoot.identifier("structure_loot/" + structure.getPath())
        );
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, @NotNull String id) {
        save(recipeOutput, ResourceKey.create(Registries.RECIPE, StructureLoot.identifier("structures/" + id)));
    }

    @Override
    public void save(RecipeOutput recipeOutput, @NotNull ResourceKey<Recipe<?>> resourceKey) {
        Advancement.Builder builder = Advancement.Builder.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceKey))
                .rewards(AdvancementRewards.Builder.recipe(resourceKey))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(builder::addCriterion);
        StructureLootRecipe structureLootRecipe = new StructureLootRecipe(this.structure, this.lootTables, this.rolls);
        recipeOutput.accept(resourceKey, structureLootRecipe, builder.build(resourceKey.identifier().withPrefix("recipes/structures/")));

    }

}
