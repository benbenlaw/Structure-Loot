package com.benbenlaw.structureloot.util;

import com.benbenlaw.structureloot.item.SLItems;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

class CuriosCompat {

    static boolean hasInCurios(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(SLItems.TOKEN_CHARM.get()))
                .orElse(false);
    }
}