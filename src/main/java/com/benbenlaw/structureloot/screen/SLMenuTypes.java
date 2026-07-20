package com.benbenlaw.structureloot.screen;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.screen.custom.StructureLootMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SLMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =  DeferredRegister.create(BuiltInRegistries.MENU, StructureLoot.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<StructureLootMenu>> STRUCTURE_LOOT_MENU =
            MENUS.register("structure_loot_menu", () -> IMenuTypeExtension.create(StructureLootMenu::new));


}
