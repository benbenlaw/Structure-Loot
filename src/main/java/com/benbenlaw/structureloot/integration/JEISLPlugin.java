package com.benbenlaw.structureloot.integration;

import com.benbenlaw.structureloot.StructureLoot;
import com.benbenlaw.structureloot.item.SLItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class JEISLPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return StructureLoot.identifier("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(SLItems.BLOCK_TOKEN.get(), new ItemSubtypeInterpreter());
        registration.registerSubtypeInterpreter(SLItems.ENTITY_TOKEN.get(), new ItemSubtypeInterpreter());
        registration.registerSubtypeInterpreter(SLItems.STRUCTURE_TOKEN.get(), new ItemSubtypeInterpreter());
    }
}
