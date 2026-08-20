package com.benbenlaw.structureloot.util;

import com.benbenlaw.structureloot.item.SLItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

public class CharmUtil {

    private static final String CURIOS_MOD_ID = "curios";
    private static final boolean CURIOS_LOADED = ModList.get().isLoaded(CURIOS_MOD_ID);

    public static boolean hasLootCharm(Player player) {
        if (CURIOS_LOADED && CuriosCompat.hasInCurios(player)) {
            return true;
        }
        return hasInInventory(player);
    }

    private static boolean hasInInventory(Player player) {
        for (var stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(SLItems.TOKEN_CHARM.get())) return true;
        }
        return false;
    }
}