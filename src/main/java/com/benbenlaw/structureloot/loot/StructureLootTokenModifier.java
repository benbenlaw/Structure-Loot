package com.benbenlaw.structureloot.loot;

import com.benbenlaw.structureloot.item.SLDataComponents;
import com.benbenlaw.structureloot.item.SLItems;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class StructureLootTokenModifier extends LootModifier {

    public static final MapCodec<StructureLootTokenModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst)
                    .and(Codec.DOUBLE.optionalFieldOf("chance", 0.1).forGetter(m -> m.chance))
                    .apply(inst, StructureLootTokenModifier::new));

    private final double chance;

    public StructureLootTokenModifier(LootItemCondition[] conditions, int priority, double chance) {
        super(conditions, priority);
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ServerLevel level = context.getLevel();
        if (level.getServer() == null) return generatedLoot;

        Identifier rolledTableId = context.getQueriedLootTableId();

        RecipeHolder<StructureLootRecipe> match = level.getServer().getRecipeManager()
                .recipeMap()
                .values()
                .stream()
                .filter(holder -> holder.value().getType() == StructureLootRecipe.TYPE)
                .map(holder -> (RecipeHolder<StructureLootRecipe>) holder)
                .filter(holder -> holder.value().lootTables().contains(rolledTableId))
                .findFirst()
                .orElse(null);

        if (match == null) return generatedLoot;

        if (context.getRandom().nextDouble() < this.chance) {
            ItemStack token = new ItemStack(SLItems.STRUCTURE_TOKEN.get());
            token.set(SLDataComponents.STRUCTURE_ID.get(), match.value().structure());
            generatedLoot.add(token);
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}