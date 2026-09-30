package com.benbenlaw.structureloot.data.custom;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe.LootRoll;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructureLootRecipeBuilder implements RecipeBuilder {

    public static final double DEFAULT_OBTAINED_CHANCE = 0.1;
    public static final double BLOCK_OBTAINED_CHANCE = 0.01;

    protected String group;
    protected Identifier lootId;
    protected List<LootRoll> lootTables;
    protected int rolls;
    protected int duration;
    protected int rfPerTick;
    protected int maxDurability;
    protected double obtainedChance;
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    public StructureLootRecipeBuilder(Identifier lootId, List<LootRoll> lootTables, int rolls, int duration, int rfPerTick, int maxDurability, double obtainedChance) {
        this.lootId = lootId;
        this.lootTables = lootTables;
        this.rolls = rolls;
        this.duration = duration;
        this.rfPerTick = rfPerTick;
        this.maxDurability = maxDurability;
        this.obtainedChance = obtainedChance;
    }

    public static StructureLootRecipeBuilder structureLootRecipe(Identifier lootId, List<LootRoll> lootTables, int rolls, int duration, int rfPerTick, int maxDurability, double obtainedChance) {
        return new StructureLootRecipeBuilder(lootId, lootTables, rolls, duration, rfPerTick, maxDurability, obtainedChance);
    }

    public static StructureLootRecipeBuilder structureLootRecipe(Identifier lootId, List<LootRoll> lootTables, int rolls, int duration, int rfPerTick, int maxDurability) {
        return structureLootRecipe(lootId, lootTables, rolls, duration, rfPerTick, maxDurability, DEFAULT_OBTAINED_CHANCE);
    }

    public static StructureLootRecipeBuilder structureLootRecipe(Identifier lootId, List<LootRoll> lootTables, int rolls, int duration, int rfPerTick) {
        return structureLootRecipe(lootId, lootTables, rolls, duration, rfPerTick, 10, DEFAULT_OBTAINED_CHANCE);
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
        Identifier structure = this.lootId;
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
        StructureLootRecipe structureLootRecipe = new StructureLootRecipe(this.lootId, this.lootTables, this.rolls, this.duration, this.rfPerTick, this.maxDurability, this.obtainedChance);
        recipeOutput.accept(resourceKey, structureLootRecipe, builder.build(resourceKey.identifier().withPrefix("recipes/structures/")));
    }
}