package com.benbenlaw.structureloot.event;

import com.benbenlaw.structureloot.StructureLoot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

@EventBusSubscriber(modid = StructureLoot.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onLootGenerated(PlayerContainerEvent event) {


    }


}
