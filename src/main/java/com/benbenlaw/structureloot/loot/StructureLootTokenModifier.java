package com.benbenlaw.structureloot.loot;

import com.benbenlaw.structureloot.item.SLDataComponents;
import com.benbenlaw.structureloot.item.SLItems;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import com.benbenlaw.structureloot.util.CharmUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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

    private record Match(StructureLootRecipe recipe, StructureLootRecipe.LootRoll roll) {}

    private Player findPlayer(LootContext context, StructureLootRecipe.LootContextType type) {
        return switch (type) {
            case BLOCK -> {
                Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
                yield entity instanceof Player player ? player : null;
            }
            case ENTITY -> {
                Entity killer = context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
                yield killer instanceof Player player ? player : null;
            }
            case GENERIC -> null;
        };
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ServerLevel level = context.getLevel();

        Identifier rolledTableId = context.getQueriedLootTableId();

        Match match = level.getServer().getRecipeManager()
                .recipeMap()
                .values()
                .stream()
                .filter(holder -> holder.value().getType() == StructureLootRecipe.TYPE)
                .map(holder -> (RecipeHolder<StructureLootRecipe>) holder)
                .flatMap(holder -> holder.value().lootTables().stream()
                        .filter(roll -> roll.table().equals(rolledTableId))
                        .map(roll -> new Match(holder.value(), roll)))
                .findFirst()
                .orElse(null);

        if (match == null) return generatedLoot;
        if (!match.recipe().canBeObtained()) return generatedLoot;

        if (match.roll().type() != StructureLootRecipe.LootContextType.GENERIC) {
            Player player = findPlayer(context, match.roll().type());
            if (player == null || !CharmUtil.hasLootCharm(player)) {
                return generatedLoot;
            }
        }

        if (context.getRandom().nextDouble() < this.chance) {
            ItemStack token = createToken(match.roll().type());
            token.set(SLDataComponents.LOOT_ID.get(), match.recipe().lootId());
            token.set(DataComponents.MAX_DAMAGE, match.recipe().maxDurability());
            generatedLoot.add(token);
        }

        return generatedLoot;
    }

    private ItemStack createToken(StructureLootRecipe.LootContextType type) {
        return switch (type) {
            case GENERIC -> new ItemStack(SLItems.STRUCTURE_TOKEN.get());
            case BLOCK -> new ItemStack(SLItems.BLOCK_TOKEN.get());
            case ENTITY -> new ItemStack(SLItems.ENTITY_TOKEN.get());
        };
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}