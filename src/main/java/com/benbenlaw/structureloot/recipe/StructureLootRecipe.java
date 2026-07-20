package com.benbenlaw.structureloot.recipe;

import com.benbenlaw.core.recipe.NoInventoryRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record StructureLootRecipe(Identifier structure, List<Identifier> lootTables, int rolls, int duration, int rfPerTick) implements Recipe<NoInventoryRecipe> {

    public static final MapCodec<StructureLootRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("structure").forGetter(StructureLootRecipe::structure),
                    Identifier.CODEC.listOf().fieldOf("loot_tables").forGetter(StructureLootRecipe::lootTables),
                    Codec.INT.fieldOf("rolls").forGetter(StructureLootRecipe::rolls),
                    Codec.INT.fieldOf("duration").forGetter(StructureLootRecipe::duration),
                    Codec.INT.fieldOf("rf_per_tick").forGetter(StructureLootRecipe::rfPerTick)
            ).apply(instance, StructureLootRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StructureLootRecipe> STREAM_CODEC = StreamCodec.of(
            StructureLootRecipe::write, StructureLootRecipe::read);

    public static final RecipeType<StructureLootRecipe> TYPE = new RecipeType<>() {};

    public static final RecipeSerializer<StructureLootRecipe> SERIALIZER =
            new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private static StructureLootRecipe read(RegistryFriendlyByteBuf buffer) {
        Identifier structure = Identifier.STREAM_CODEC.decode(buffer);
        List<Identifier> lootTables = Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        int roles = buffer.readInt();
        int duration = buffer.readInt();
        int rfPerTick = buffer.readInt();
        return new StructureLootRecipe(structure, lootTables, roles, duration, rfPerTick);
    }

    private static void write(RegistryFriendlyByteBuf buffer, StructureLootRecipe recipe) {
        Identifier.STREAM_CODEC.encode(buffer, recipe.structure);
        Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.lootTables);
        buffer.writeInt(recipe.rolls);
        buffer.writeInt(recipe.duration);
        buffer.writeInt(recipe.rfPerTick);
    }
    @Override
    public boolean matches(@NotNull NoInventoryRecipe container, @NotNull Level level) {
        return true;
    }

    //Boiler Plate
    @Override
    public @NonNull ItemStack assemble(NoInventoryRecipe recipeInput) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<NoInventoryRecipe>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<NoInventoryRecipe>> getType() {
        return TYPE;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NonNull String group() {
        return "";
    }
}
