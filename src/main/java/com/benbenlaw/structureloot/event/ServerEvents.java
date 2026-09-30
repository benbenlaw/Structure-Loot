package com.benbenlaw.structureloot.event;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.block.SLBlockEntities;
import com.benbenlaw.structureloot.event.client.ClientRecipeCache;
import com.benbenlaw.structureloot.recipe.SLRecipeTypes;
import com.benbenlaw.structureloot.recipe.StructureLootRecipe;
import com.benbenlaw.structureloot.loot.LootPreviewBuilder;
import com.benbenlaw.structureloot.network.packet.LootPreviewPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = StructureLoot.MOD_ID)
public class ServerEvents {

    private static Object cachedFor;
    private static Map<Identifier, List<LootPreviewPacket.Drop>> cachedDrops;

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

    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(SLRecipeTypes.STRUCTURE_LOOT_TYPE.get());

        MinecraftServer server = event.getPlayerList().getServer();
        if (cachedFor != server.reloadableRegistries() || cachedDrops == null) {
            cachedFor = server.reloadableRegistries();
            cachedDrops = LootPreviewBuilder.build(server);
        }
        List<LootPreviewPacket> packets = new java.util.ArrayList<>();
        int index = 0;
        for (Map.Entry<Identifier, List<LootPreviewPacket.Drop>> entry : cachedDrops.entrySet()) {
            packets.add(new LootPreviewPacket(index++, cachedDrops.size(), entry.getKey(), entry.getValue()));
        }
        event.getRelevantPlayers().forEach(player -> packets.forEach(packet -> PacketDistributor.sendToPlayer(player, packet)));
    }

    @SubscribeEvent
    public static void onRecipeReceived(RecipesReceivedEvent event) {
        RecipeMap recipeMap = event.getRecipeMap();

        Collection<RecipeHolder<StructureLootRecipe>> meltingRecipe = recipeMap.byType(SLRecipeTypes.STRUCTURE_LOOT_TYPE.get());
        Map<Identifier, StructureLootRecipe> laserRecipeMap = new HashMap<>();

        for (RecipeHolder<StructureLootRecipe> recipeHolder : meltingRecipe) {
            laserRecipeMap.put(recipeHolder.id().identifier(), recipeHolder.value());
        }
        ClientRecipeCache.setCachedStructureLootRecipes(laserRecipeMap);
    }
}
