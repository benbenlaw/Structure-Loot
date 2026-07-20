package com.benbenlaw.structureloot.event;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.SLBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

@EventBusSubscriber(modid = StructureLoot.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        //Structure Loot Block Entity
        event.registerBlockEntity(Capabilities.Item.BLOCK, SLBlockEntities.STRUCTURE_LOOT_BLOCK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.getItemHandler());

        event.registerBlockEntity(Capabilities.Energy.BLOCK, SLBlockEntities.STRUCTURE_LOOT_BLOCK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.getEnergyHandler());

        //Structure Loot Part Block Entity
        event.registerBlockEntity(Capabilities.Item.BLOCK, SLBlockEntities.STRUCTURE_LOOT_PART_BLOCK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.getItemHandler());

        event.registerBlockEntity(Capabilities.Energy.BLOCK, SLBlockEntities.STRUCTURE_LOOT_PART_BLOCK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.getEnergyHandler());

    }
}
