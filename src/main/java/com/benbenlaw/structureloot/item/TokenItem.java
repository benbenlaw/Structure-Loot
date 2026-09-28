package com.benbenlaw.structureloot.item;

import com.benbenlaw.structureloot.event.client.ClientEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class TokenItem extends Item {

    public TokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack itemStack) {

        if (itemStack.is(SLItems.STRUCTURE_TOKEN) && itemStack.has(SLDataComponents.LOOT_ID.get())) {
            String structure = formatStructureName(Objects.requireNonNull(itemStack.get(SLDataComponents.LOOT_ID.get())));
            return Component.translatable("item.structureloot.structure_token", structure);
        }

        if (itemStack.is(SLItems.BLOCK_TOKEN) && itemStack.has(SLDataComponents.LOOT_ID.get())) {
            String block = BuiltInRegistries.BLOCK.getValue(Objects.requireNonNull(itemStack.get(SLDataComponents.LOOT_ID.get()))).getName().getString();
            return Component.translatable("item.structureloot.block_token", block);
        }

        if (itemStack.is(SLItems.ENTITY_TOKEN) && itemStack.has(SLDataComponents.LOOT_ID.get())) {
            String entity = BuiltInRegistries.ENTITY_TYPE.getValue(Objects.requireNonNull(itemStack.get(SLDataComponents.LOOT_ID.get()))).getDescription().getString();
            return Component.translatable("item.structureloot.entity_token", entity);
        }

        return super.getName(itemStack);
    }

    public static String formatStructureName(Identifier id) {
        String path = id.getPath();
        return Arrays.stream(path.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
