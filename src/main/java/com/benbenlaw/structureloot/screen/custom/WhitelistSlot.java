//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.benbenlaw.structureloot.screen.custom;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class WhitelistSlot extends ResourceHandlerSlot {
    private final TagKey<Item> tagKey;

    public WhitelistSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, int index, int xPosition, int yPosition, TagKey<Item> tagKey) {
        super(handler, slotModifier, index, xPosition, yPosition);
        this.tagKey = tagKey;
    }

    public boolean mayPlace(ItemStack stack) {
        return stack.is(tagKey);
    }
}
