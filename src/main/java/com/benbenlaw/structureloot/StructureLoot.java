package com.benbenlaw.structureloot;

import com.benbenlaw.structureloot.block.SLBlockEntities;
import com.benbenlaw.structureloot.block.SLBlocks;
import com.benbenlaw.structureloot.block.entity.renderer.StructureLootBlockEntityRenderer;
import com.benbenlaw.structureloot.block.entity.renderer.StructureLootModel;
import com.benbenlaw.structureloot.block.entity.renderer.StructureLootModelLayers;
import com.benbenlaw.structureloot.data.SLLootModifierProvider;
import com.benbenlaw.structureloot.item.SLDataComponents;
import com.benbenlaw.structureloot.item.SLItems;
import com.benbenlaw.structureloot.loot.SLLootModifiers;
import com.benbenlaw.structureloot.network.SLMessages;
import com.benbenlaw.structureloot.recipe.SLRecipeTypes;
import com.benbenlaw.structureloot.screen.SLMenuTypes;
import com.benbenlaw.structureloot.screen.custom.StructureLootScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(StructureLoot.MOD_ID)
public class StructureLoot {

    public static final String MOD_ID = "structureloot";
    private static final Logger LOGGER = LogManager.getLogger();


    public StructureLoot(IEventBus modEventBus, final ModContainer modContainer) {

        SLBlocks.BLOCKS.register(modEventBus);
        SLItems.ITEMS.register(modEventBus);
        SLDataComponents.COMPONENTS.register(modEventBus);
        SLBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        SLMenuTypes.MENUS.register(modEventBus);
        SLRecipeTypes.SERIALIZER.register(modEventBus);
        SLRecipeTypes.TYPES.register(modEventBus);
        SLLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(this::networkingSetup);
    }



    @EventBusSubscriber(modid = MOD_ID)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(SLBlockEntities.STRUCTURE_LOOT_BLOCK_ENTITY.get(), StructureLootBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(StructureLootModelLayers.STRUCTURE_LOOT_MAIN, StructureLootModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(SLMenuTypes.STRUCTURE_LOOT_MENU.get(), StructureLootScreen::new);
        }
    }

    public void networkingSetup(RegisterPayloadHandlersEvent event) {
        SLMessages.registerNetworking(event);
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}

