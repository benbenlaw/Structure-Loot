package com.benbenlaw.structureloot.event.client;

import com.benbenlaw.core.util.TooltipUtil;
import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.SLBlocks;
import com.benbenlaw.structureloot.block.entity.renderer.StructureLootSpecialRenderer;
import com.benbenlaw.structureloot.item.SLDataComponents;
import com.benbenlaw.structureloot.item.SLItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = StructureLoot.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerSpecialModel(RegisterSpecialModelRendererEvent event) {
        event.register(StructureLoot.identifier("block/structure_loot_block"), StructureLootSpecialRenderer.Unbaked.CODEC);
    }

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        TooltipUtil.addShiftTooltip(stack, event, SLBlocks.STRUCTURE_LOOT_BLOCK.get().asItem(), "tooltip.structureloot.structure_loot_block");
        TooltipUtil.addShiftTooltip(stack, event, SLItems.TOKEN_CHARM.get().asItem(), "tooltip.structureloot.token_charm");

        if (stack.has(SLDataComponents.LOOT_ID.get())) {
            Identifier lootId = stack.get(SLDataComponents.LOOT_ID.get());

            assert lootId != null;
            String lootName = formatStructureName(lootId);

            TokenLabels labels = getTokenLabels(stack);

            if (Minecraft.getInstance().hasShiftDown()) {
                event.getToolTip().add(Component.translatable(labels.headerKey()).withStyle(ChatFormatting.BLUE));
                event.getToolTip().add(Component.translatable(labels.detailKey(), lootName).withStyle(ChatFormatting.BLUE));
            } else {
                event.getToolTip().add(Component.translatable("tooltip.bblcore.shift").withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    private record TokenLabels(String headerKey, String detailKey) {}

    private static TokenLabels getTokenLabels(ItemStack stack) {
        if (stack.is(SLItems.BLOCK_TOKEN.get())) {
            return new TokenLabels("tooltip.structureloot.block_token", "tooltip.structureloot.block");
        } else if (stack.is(SLItems.ENTITY_TOKEN.get())) {
            return new TokenLabels("tooltip.structureloot.entity_token", "tooltip.structureloot.entity");
        } else {
            return new TokenLabels("tooltip.structureloot.structure_token", "tooltip.structureloot.structure");
        }
    }

    public static String formatStructureName(Identifier id) {
        String path = id.getPath();
        return Arrays.stream(path.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
