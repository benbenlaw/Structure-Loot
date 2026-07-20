package com.benbenlaw.structureloot.data;

import com.benbenlaw.structureloot.StructureLoot;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class SLLangProvider extends LanguageProvider {

    public SLLangProvider(PackOutput output) {
        super(output, StructureLoot.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        //Creative Tab
        add("itemGroup.structureloot", "Structure Loot");

        //Items
        add("item.structureloot.structure_token", "Structure Token");

        //Tooltips
        add("tooltip.structureloot.structure", "Structure: %s");

    }

}


