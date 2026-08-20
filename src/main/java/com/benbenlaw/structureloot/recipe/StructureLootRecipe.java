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
import java.util.Optional;

public record StructureLootRecipe(Identifier lootId, List<StructureLootRecipe.LootRoll> lootTables,
                                  int rolls, int duration, int rfPerTick, int maxDurability,
                                  boolean canBeObtained) implements Recipe<NoInventoryRecipe> {

    public static final MapCodec<StructureLootRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("loot_id").forGetter(StructureLootRecipe::lootId),
                    LootRoll.CODEC.listOf().fieldOf("loot_tables").forGetter(StructureLootRecipe::lootTables),
                    Codec.INT.fieldOf("rolls").forGetter(StructureLootRecipe::rolls),
                    Codec.INT.fieldOf("duration").forGetter(StructureLootRecipe::duration),
                    Codec.INT.fieldOf("rf_per_tick").forGetter(StructureLootRecipe::rfPerTick),
                    Codec.INT.optionalFieldOf("max_durability", 10).forGetter(StructureLootRecipe::maxDurability),
                    Codec.BOOL.optionalFieldOf("can_be_obtained", true).forGetter(StructureLootRecipe::canBeObtained)
            ).apply(instance, StructureLootRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StructureLootRecipe> STREAM_CODEC = StreamCodec.of(
            StructureLootRecipe::write, StructureLootRecipe::read);

    public static final RecipeType<StructureLootRecipe> TYPE = new RecipeType<>() {};

    public static final RecipeSerializer<StructureLootRecipe> SERIALIZER =
            new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private static StructureLootRecipe read(RegistryFriendlyByteBuf buffer) {
        Identifier lootId = Identifier.STREAM_CODEC.decode(buffer);
        List<LootRoll> lootTables = LootRoll.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        int rolls = buffer.readInt();
        int duration = buffer.readInt();
        int rfPerTick = buffer.readInt();
        int maxDurability = buffer.readInt();
        boolean canBeObtained = buffer.readBoolean();
        return new StructureLootRecipe(lootId, lootTables, rolls, duration, rfPerTick, maxDurability, canBeObtained);
    }

    private static void write(RegistryFriendlyByteBuf buffer, StructureLootRecipe recipe) {
        Identifier.STREAM_CODEC.encode(buffer, recipe.lootId);
        LootRoll.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.lootTables);
        buffer.writeInt(recipe.rolls);
        buffer.writeInt(recipe.duration);
        buffer.writeInt(recipe.rfPerTick);
        buffer.writeInt(recipe.maxDurability);
        buffer.writeBoolean(recipe.canBeObtained);
    }

    @Override
    public boolean matches(@NotNull NoInventoryRecipe container, @NotNull Level level) {
        return true;
    }

    public record LootRoll(Identifier table, LootContextType type,
                           Optional<Identifier> blockId, Optional<Identifier> entityId) {

        public static LootRoll generic(Identifier table) {
            return new LootRoll(table, LootContextType.GENERIC, Optional.empty(), Optional.empty());
        }

        public static LootRoll block(Identifier table, Identifier blockId) {
            return new LootRoll(table, LootContextType.BLOCK, Optional.of(blockId), Optional.empty());
        }

        public static LootRoll entity(Identifier table, Identifier entityId) {
            return new LootRoll(table, LootContextType.ENTITY, Optional.empty(), Optional.of(entityId));
        }

        public static final Codec<LootRoll> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("table").forGetter(LootRoll::table),
                        LootContextType.CODEC.optionalFieldOf("type", LootContextType.GENERIC).forGetter(LootRoll::type),
                        Identifier.CODEC.optionalFieldOf("block").forGetter(LootRoll::blockId),
                        Identifier.CODEC.optionalFieldOf("entity").forGetter(LootRoll::entityId)
                ).apply(instance, LootRoll::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, LootRoll> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, LootRoll::table,
                LootContextType.STREAM_CODEC, LootRoll::type,
                ByteBufCodecs.optional(Identifier.STREAM_CODEC), LootRoll::blockId,
                ByteBufCodecs.optional(Identifier.STREAM_CODEC), LootRoll::entityId,
                LootRoll::new
        );
    }

    public enum LootContextType {
        GENERIC,
        BLOCK,
        ENTITY;

        public static final Codec<LootContextType> CODEC =
                Codec.STRING.xmap(LootContextType::fromString, LootContextType::toName);

        public static LootContextType fromString(String name) {
            for (LootContextType type : values()) {
                if (type.name().equalsIgnoreCase(name)) return type;
            }
            return GENERIC;
        }

        public String toName() {
            return name().toLowerCase();
        }

        public static final StreamCodec<RegistryFriendlyByteBuf, LootContextType> STREAM_CODEC =
                StreamCodec.of(
                        (buf, type) -> buf.writeEnum(type),
                        buf -> buf.readEnum(LootContextType.class)
                );
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