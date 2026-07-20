package com.benbenlaw.structureloot.event.client;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.item.SLDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = StructureLoot.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.has(SLDataComponents.STRUCTURE_ID.get())) {
            Identifier structure = stack.get(SLDataComponents.STRUCTURE_ID.get());

            assert structure != null;
            String structureName = formatStructureName(structure);

            if (Minecraft.getInstance().hasShiftDown()) {
                event.getToolTip().add(Component.translatable("tooltip.structureloot.structure", structureName).withStyle(ChatFormatting.BLUE));

            } else {
                event.getToolTip().add(Component.translatable("tooltip.bblcore.shift").withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    public static String formatStructureName(Identifier id) {
        String path = id.getPath();
        return Arrays.stream(path.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
